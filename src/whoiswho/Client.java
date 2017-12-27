package whoiswho;

import java.io.*;
import java.net.Socket;

public class Client {




    public static void main(String[] args) throws IOException {
        Socket socket;
        socket = new Socket("localhost", 5555);
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));


        while(true){

            System.out.println(reader.readLine());
            System.out.println(input.readLine());

        }
    }


}
