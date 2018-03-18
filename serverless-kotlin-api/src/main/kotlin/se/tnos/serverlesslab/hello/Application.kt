package se.tnos.serverlesslab.hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.io.IOException
import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import java.io.InputStream
import java.io.OutputStream


@SpringBootApplication
@EnableWebMvc
class Application : SpringBootServletInitializer() {

    @Bean
    fun helloWorldController(): HelloWorldController {
        return HelloWorldController()
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@RestController
class HelloWorldController {

    @RequestMapping("/hello")
    fun hello(@RequestParam("name") name: String): String {
        return "Hello $name"
    }
}

class StreamLambdaHandler : RequestStreamHandler {

    @Throws(IOException::class)
    override fun handleRequest(inputStream: InputStream, outputStream: OutputStream, context: Context) {
        handler!!.proxyStream(inputStream, outputStream, context)

        // just in case it wasn't closed by the mapper
        outputStream.close()
    }

    companion object {
        private var handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse>? = null

        init {
            try {
                handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application::class.java)
            } catch (e: ContainerInitializationException) {
                // if we fail here. We re-throw the exception to force another cold start
                e.printStackTrace()
                throw RuntimeException("Could not initialize Spring Boot application", e)
            }

        }
    }
}


