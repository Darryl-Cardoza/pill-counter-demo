import android.content.Context
import android.util.Base64
import android.util.Log
import com.rite.pillcounting.core.hl7.imageWebService.TlsImageKeystoreUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.json.JSONObject
import java.io.File

class ImageWebServer(private val context: Context) {

    private var server: ApplicationEngine? = null

    companion object {
        private const val PORT = 8443
        private const val TAG = "ImageWebServer"
    }

    fun start() {
        if (server != null) return

        val keyStore = TlsImageKeystoreUtil.ensureKeystore(context)

        server = embeddedServer(
            Netty,
            environment = applicationEngineEnvironment {

                sslConnector(
                    keyStore = keyStore,
                    keyAlias = TlsImageKeystoreUtil.alias(),
                    keyStorePassword = { TlsImageKeystoreUtil.password() },
                    privateKeyPassword = { TlsImageKeystoreUtil.password() }
                ) {
                    port = PORT
                    host = "0.0.0.0"
                    enabledProtocols = listOf("TLSv1.2", "TLSv1.3")
                }

                module {
                    routing {

                        get("/health") {
                            call.respondText("""{"status":"ok"}""")
                        }

                        get("/fingerprint") {
                            call.respondText(
                                JSONObject()
                                    .put("fingerprint", TlsImageKeystoreUtil.fingerprint(context as Context))
                                    .toString(),
                                ContentType.Application.Json
                            )
                        }

                        get("/images/{file}") {
                            serveRoomImage(call)
                        }
                    }
                }
            }
        ).start(wait = false)

        Log.i(TAG, "HTTPS Image Server started on port $PORT")
        Log.i(TAG, "Cert fingerprint: ${TlsImageKeystoreUtil.fingerprint(context)}")
    }

    fun stop() {
        server?.stop(2000, 5000)
        server = null
    }

    private suspend fun serveRoomImage(call: ApplicationCall) {
        val fileName = call.parameters["file"]
            ?: return call.badRequest("Missing file name")

        if (!isSafeFileName(fileName)) {
            return call.badRequest("Invalid file name")
        }

        val searchRoots = listOfNotNull(
            context.filesDir,
            context.cacheDir,
            context.getExternalFilesDir(null),
            context.getExternalFilesDir("Pictures")
        )

        val imageFile = searchRoots
            .flatMap { it.walkTopDown().toList() }
            .firstOrNull { it.name == fileName }

        if (imageFile == null || !imageFile.exists()) {
            return call.respondText(
                errorJson("Image not found"),
                ContentType.Application.Json,
                HttpStatusCode.NotFound
            )
        }

        val bytes = imageFile.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        call.respondText(
            json {
                put("success", true)
                put("file", fileName)
                put("base64", base64)
            },
            ContentType.Application.Json,
            HttpStatusCode.OK
        )
    }


    // ------------------------------------------------------------
    private fun isSafeFileName(name: String): Boolean =
        name.isNotBlank() &&
                !name.contains("..") &&
                !name.contains("/") &&
                !name.contains("\\")

    private suspend fun ApplicationCall.badRequest(msg: String) {
        respondText(
            errorJson(msg),
            ContentType.Application.Json,
            HttpStatusCode.BadRequest
        )
    }

    private fun errorJson(msg: String): String =
        JSONObject()
            .put("success", false)
            .put("error", msg)
            .toString()

    private fun json(block: JSONObject.() -> Unit): String =
        JSONObject().apply(block).toString()
}
