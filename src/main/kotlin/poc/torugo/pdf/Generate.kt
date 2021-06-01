package poc.torugo.pdf

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
// import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import org.jboss.resteasy.annotations.jaxrs.FormParam
import org.jboss.resteasy.annotations.providers.multipart.*
import java.io.*
import javax.ws.rs.*
import javax.ws.rs.core.*






class Body {
    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    var file: InputStream? = null
}

@Path("/convert/html")
class Generate {
    val producer : String = "afyadigital.maagizo"

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    fun hello(@MultipartForm body: Body ): Response {
        val file = body.file ?: return Response.status(400, "empty file").build()
        val reader = file?.bufferedReader()

        val content = StringBuilder()
        reader.use { reader ->
            var line = reader?.readLine()
            while (line != null) {
                content.append(line)
                line = reader?.readLine()
            }
        }

        try {
            val stream = ByteArrayOutputStream()
            val builder = PdfRendererBuilder()
            builder.useFastMode()
            builder.usePdfUaAccessbility(true)
            builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_3_A)
            // builder.useSVGDrawer(BatikSVGDrawer())
            builder.withHtmlContent(content.toString(), null)
            val fontURL = Generate::class.java.getResource("arial.ttf") ?: throw Exception("no font found")
            builder.useFont(File(fontURL.toURI()), "Arial")
            builder.toStream(stream);
            builder.withProducer(this.producer)
            builder.run();
            val response = Response.ok(stream.toByteArray())
            response.header("Content-Disposition", "attachment;filename=prescription.pdf")
            return response.build()
        } catch (e : Exception) {
            e.printStackTrace()
            return Response.serverError().build()
        }
    }
}