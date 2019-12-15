package com.android.example.flow.twitter.hmac_sha1

/**
 * Created by Santanu 😁 on 2019-11-19.
 */
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.internal.network.UrlUtils
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants
import okio.ByteString
import java.io.UnsupportedEncodingException
import java.net.URI
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class OAuth1aParameters(
    private val authConfig: TwitterAuthConfig,
    private val authToken: TwitterAuthToken?,
    private val callback: String?,
    private val method: String,
    private val url: String,
    private val postParams: Map<String, String>?
) {

    /**
     * @return the authorization header for inclusion in HTTP request headers for a request token.
     */
    val authorizationHeader: String
        get() {
            val nonce = nonce
            val timestamp = timestamp
            val signatureBase = constructSignatureBase(nonce, timestamp)
            val signature = calculateSignature(signatureBase)
            return constructAuthorizationHeader(nonce, timestamp, signature)
        }

    private val nonce: String
        get() = System.nanoTime().toString() + Math.abs(RAND.nextLong()).toString()

    private val timestamp: String
        get() {
            val secondsFromEpoch = System.currentTimeMillis() / 1000
            return secondsFromEpoch.toString()
        }

    private val signingKey: String
        get() {
            val tokenSecret = authToken?.secret
            return StringBuilder()
                .append(UrlUtils.urlEncode(authConfig.consumerSecret))
                .append('&')
                .append(UrlUtils.urlEncode(tokenSecret))
                .toString()
        }

    private fun constructSignatureBase(nonce: String, timestamp: String): String {
        // Get query parameters from request.
        val uri = URI.create(url)
        val params = UrlUtils.getQueryParams(uri, true)
        if (postParams != null) {
            params.putAll(postParams)
        }

        // Add OAuth parameters.
        if (callback != null) {
            params[OAuthConstants.PARAM_CALLBACK] = callback
        }
        params[OAuthConstants.PARAM_CONSUMER_KEY] = authConfig.consumerKey
        params[OAuthConstants.PARAM_NONCE] = nonce
        params[OAuthConstants.PARAM_SIGNATURE_METHOD] = SIGNATURE_METHOD
        params[OAuthConstants.PARAM_TIMESTAMP] = timestamp
        if (authToken != null && authToken.token != null) {
            params[OAuthConstants.PARAM_TOKEN] = authToken.token
        }
        params[OAuthConstants.PARAM_VERSION] = VERSION

        // Construct the signature base.
        val baseUrl = uri.scheme + "://" + uri.host + uri.path
        val sb = StringBuilder()
            .append(method.toUpperCase(Locale.ENGLISH))
            .append('&')
            .append(UrlUtils.percentEncode(baseUrl))
            .append('&')
            .append(getEncodedQueryParams(params))
        return sb.toString()
    }

    private fun getEncodedQueryParams(params: TreeMap<String, String>): String {
        val paramsBuf = StringBuilder()
        val numParams = params.size
        var current = 0
        for ((key, value) in params) {
            paramsBuf.append(UrlUtils.percentEncode(UrlUtils.percentEncode(key)))
                .append("%3D")
                .append(UrlUtils.percentEncode(UrlUtils.percentEncode(value)))
            current += 1
            if (current < numParams) {
                paramsBuf.append("%26")
            }
        }
        return paramsBuf.toString()
    }

    private fun calculateSignature(signatureBase: String): String {
        try {
            val key = signingKey
            // Calculate the signature by passing both the signature base and signing key to the
            // HMAC-SHA1 hashing algorithm
            val signatureBaseBytes = signatureBase.toByteArray(charset(UrlUtils.UTF8))
            val keyBytes = key.toByteArray(charset(UrlUtils.UTF8))
            val secretKey = SecretKeySpec(keyBytes, "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(secretKey)
            val signatureBytes = mac.doFinal(signatureBaseBytes)
            return ByteString.of(signatureBytes, 0, signatureBytes.size).base64()
        } catch (e: InvalidKeyException) {
            Twitter.getLogger().e(TwitterCore.TAG, "Failed to calculate signature", e)
            return ""
        } catch (e: NoSuchAlgorithmException) {
            Twitter.getLogger().e(TwitterCore.TAG, "Failed to calculate signature", e)
            return ""
        } catch (e: UnsupportedEncodingException) {
            Twitter.getLogger().e(TwitterCore.TAG, "Failed to calculate signature", e)
            return ""
        }

    }

    private fun constructAuthorizationHeader(
        nonce: String,
        timestamp: String,
        signature: String
    ): String {
        val sb = StringBuilder("OAuth")
        appendParameter(sb, OAuthConstants.PARAM_CALLBACK, callback)
        appendParameter(sb, OAuthConstants.PARAM_CONSUMER_KEY, authConfig.consumerKey)
        appendParameter(sb, OAuthConstants.PARAM_NONCE, nonce)
        appendParameter(sb, OAuthConstants.PARAM_SIGNATURE, signature)
        appendParameter(sb, OAuthConstants.PARAM_SIGNATURE_METHOD, SIGNATURE_METHOD)
        appendParameter(sb, OAuthConstants.PARAM_TIMESTAMP, timestamp)
        val token = authToken?.token
        appendParameter(sb, OAuthConstants.PARAM_TOKEN, token)
        appendParameter(sb, OAuthConstants.PARAM_VERSION, VERSION)
        // Remove the extra ',' at the end.
        return sb.substring(0, sb.length - 1)
    }

    private fun appendParameter(sb: StringBuilder, name: String, value: String?) {
        if (value != null) {
            sb.append(' ')
                .append(UrlUtils.percentEncode(name)).append("=\"")
                .append(UrlUtils.percentEncode(value)).append("\",")
        }
    }

    companion object {
        private const val VERSION = "1.0"
        private const val SIGNATURE_METHOD = "HMAC-SHA1"

        /**
         * Secure random number generator to sign requests.
         */
        private val RAND = SecureRandom()
    }
}