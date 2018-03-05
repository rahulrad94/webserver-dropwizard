import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.ws.rs.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.ws.rs.core.Response;

@Path("/")
public class Resource {
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
    Semaphore lock = new Semaphore(1);

    @POST
    @Path("/changeappliance")
    public String postBody(String postRequestBody) {

        String responseString = new String("");
        try {
            JsonNode request = mapper.readTree(postRequestBody);
            JsonNode componentFile = mapper.readTree(accessComponentFile());
            JsonNode componentID = request.get("id");

            for (Iterator<Map.Entry<String, JsonNode>> it = request.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                if("id".equals(entry.getKey()))
                    continue;
                JsonNode componentToChange = componentFile.get(componentID.toString().replace("\"",""));
                if(componentToChange.get(entry.getKey()) != null)
                    ((ObjectNode) componentToChange).set(entry.getKey(), request.get(entry.getKey()));
                else
                    //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    return "Missing Key";
            }
            responseString = componentFile.toString();
            lock.acquire();
            writer.writeValue(new File("src/main/java/components.json"), componentFile);
            lock.release();
        } catch (IOException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return "IO exception";
        } catch (InterruptedException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return "Lock Exception";
        }

        //return Response.ok(responseString).build();
          return responseString;
    }

    @GET
    @Path("/accesscomponent/{id}")
    public String accessComponent(@PathParam("id") String id) {

        String jsonString = accessComponentFile();

        try {
            JsonNode componentsJson = mapper.readTree(jsonString);
            //return Response.ok(componentsJson.get(id)).build();
            return String.valueOf(componentsJson.get(id));
        } catch (IOException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return "IO Exception";
        }
    }

    @GET
    @Path("/accesscomponentfile")
    public String accessComponentFile() {
        JSONParser parser = new JSONParser();
        try {
            lock.acquire();
            Object obj = parser.parse(new FileReader("src/main/java/components.json"));
            lock.release();
            JSONObject jsonObject = (JSONObject) obj;
            //return Response.ok(jsonObject.toJSONString()).build();
            return jsonObject.toJSONString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.NOT_FOUND).build();
            return "FileNotFoundException OCCURREDDDD!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        } catch (IOException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return "IOException OCCURREDDDD!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        } catch (InterruptedException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return "Lock Exception OCCURREDDDD!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        }catch (ParseException e) {
            e.printStackTrace();
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return "ParseException OCCURREDDDD!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        }
    }
}
