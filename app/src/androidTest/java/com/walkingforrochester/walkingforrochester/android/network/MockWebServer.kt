package com.walkingforrochester.walkingforrochester.android.network

import com.walkingforrochester.walkingforrochester.android.network.MockServerData.localHostCertificate
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.InetAddress

fun MockWebServer.buildHttpClient(): OkHttpClient {
    val clientCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(localHostCertificate.certificate)
        .build()

    val redactText = Regex("\"password\":\".+\"")

    val loggingInterceptor = HttpLoggingInterceptor {
        val msg = redactText.replace(it, "\"password\":\"******\"")
        HttpLoggingInterceptor.Logger.DEFAULT.log(msg)
    }.apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    return OkHttpClient.Builder()
        .sslSocketFactory(
            clientCertificates.sslSocketFactory(),
            clientCertificates.trustManager
        )
        .addInterceptor(loggingInterceptor)
        .build()
}

fun MockWebServer.installServerClientCertificate() {

    val serverCertificates = HandshakeCertificates.Builder()
        .heldCertificate(localHostCertificate)
        .build()

    this.useHttps(serverCertificates.sslSocketFactory(), false)
}

object MockServerData {
    private val localhost: String by lazy {
        InetAddress.getByName("localhost").canonicalHostName
    }

    val localHostCertificate: HeldCertificate by lazy {
        HeldCertificate.Builder()
            .addSubjectAlternativeName(localhost)
            .build()
    }
}