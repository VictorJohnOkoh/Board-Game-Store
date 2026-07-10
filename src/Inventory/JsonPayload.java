package Inventory;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;



public class JsonPayload {

    private Product product;

    public JsonPayload(Product p){
        product = p;
    }

    public void DeliverProductPayload() {
        Gson gson = new Gson();
        String json;
        String functionName = "";

        if (product.getProductCategory().equals(ProductCategory.BOARDGAME)) {
            json = gson.toJson((BoardGame) product);
            functionName = "addBoardGame";
        } else {
            json = gson.toJson((Accessory) product);
            functionName = "addAccessory";
        }
        String python;

        // determine if python or python3 is used depending on the OS
        if (System.getProperty("os.name").equalsIgnoreCase("Linux") || System.getProperty("os.name").equalsIgnoreCase("macOS") || System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
            python = "python3";
        } else {
            python = "python";
        }

        try {
            // new processbuilder that takes the python version of the PC, the DBMS script and the function to run as parameters
            ProcessBuilder pb = new ProcessBuilder(python, "src/Inventory/DatabaseManager.py", functionName);
            Process process = pb.start();

            // Writes the JSON string to python's Standard Input
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(json);
                writer.flush();
            }

            // Read any print statements back from python to see if it worked
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python script exited with exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }



}

