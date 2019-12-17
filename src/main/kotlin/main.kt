import com.cyborch.tsajson.*
import io.javalin.Javalin

fun main(args: Array<String>) {
    initialize()
    val app = Javalin.create().start(7000)
    app.get("/pub") { ctx -> ctx.result(publicKeyContent()) }
    app.get("/cert") { ctx -> ctx.result(certContent()) }
    app.get("/jwk") { ctx -> ctx.result(jwkContent()) }
    app.post("/sign") { ctx -> ctx.result(response(Request.from(ctx))) }
}
