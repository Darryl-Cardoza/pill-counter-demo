package com.example.pillcountingnewmodels.core.network

import java.io.IOException

/**
 * A sealed class representing all possible exceptions from the API layer.
 * This provides a structured and type-safe way to handle errors.
 *
 * @param message A descriptive error message.
 */
sealed class ApiException(message: String) : IOException(message) {
    /** Represents a network-level error (e.g., no internet connection, DNS issue). */
    class Network(message: String) : ApiException(message)

    /** Represents a server-side error (HTTP 5xx). */
    class ServerError(val code: Int, message: String) : ApiException(message)

    /** Represents an authentication error (HTTP 401), session has expired. */
    class Unauthorized(message: String) : ApiException(message)

    /** Represents a generic client-side error (HTTP 4xx). */
    class ClientError(val code: Int, message: String) : ApiException(message)

    /** Represents an error during JSON parsing. */
    class Parsing(message: String) : ApiException(message)
}
