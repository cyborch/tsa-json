import com.cyborch.tsajson.*
import io.javalin.Javalin

fun main(args: Array<String>) {
    initialize()
    val app = Javalin.create().start(7000)
    app.get("/pub") { ctx -> ctx.result(publicKeyContent()) }
    app.get("/cert") { ctx -> ctx.result(certContent()) }
    app.post("/sign") { ctx ->
        val request = ctx.bodyAsClass(Request::class.java)
        ctx.result(response(request))
    }
}
