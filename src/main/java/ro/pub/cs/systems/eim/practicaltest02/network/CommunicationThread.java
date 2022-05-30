package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.Valuta;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (valuta!");
            String valuta = bufferedReader.readLine();
            if (valuta == null || valuta.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (valuta!");
                return;
            }
            Valuta data = serverThread.getData();
            Valuta newData = null;
            if (data != null) {
                if(data.getLastUpdate() != null) {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                    newData = data;
                }
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";
                if(false) {
                    HttpPost httpPost = new HttpPost("https://api.coindesk.com/v1/bpi/currentprice.json");
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("mode", "json"));
                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                    httpPost.setEntity(urlEncodedFormEntity);
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();

                    pageSourceCode = httpClient.execute(httpPost, responseHandler);
                } else {
                    HttpGet httpGet = new HttpGet("https://api.coindesk.com/v1/bpi/currentprice.json");
                    HttpResponse httpGetResponse = httpClient.execute(httpGet);
                    HttpEntity httpGetEntity = httpGetResponse.getEntity();
                    if (httpGetEntity != null) {
                        pageSourceCode = EntityUtils.toString(httpGetEntity);

                    }
                }

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else
                    Log.i(Constants.TAG, pageSourceCode );

                if (false) {
                    Document document = Jsoup.parse(pageSourceCode);
                    Element element = document.child(0);
                    Elements elements = element.getElementsByTag("script");
                    for (Element script : elements) {
                        String scriptData = script.data();
                        if (scriptData.contains("wui.api_data =\n")) {
                            int position = scriptData.indexOf("wui.api_data =\n") + ("wui.api_data =\n").length();
                            scriptData = scriptData.substring(position);
                            JSONObject content = new JSONObject(scriptData);
                            JSONObject currentObservation = content.getJSONObject("current_value");
                            String euro = currentObservation.getString("EUR");
                            String dolar = currentObservation.getString("USD");
                            newData = new Valuta(
                                    euro, dolar, null
                            );
                            serverThread.setData(newData);
                            break;
                        }
                    }
                } else {
                    JSONObject content = new JSONObject(pageSourceCode);

                    JSONObject valuesArray = content.getJSONObject("bpi");

                    JSONObject main = valuesArray.getJSONObject("USD");
                    String dolar = main.getString("rate");
                    main = content.getJSONObject("EUR");
                    String euro = main.getString("rate");

                    newData = new Valuta(
                            euro, dolar, null
                    );
                    serverThread.setData(newData);
                }
            }
            if (newData == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Valuta Information is null!");
                return;
            }
            String result = null;
            switch(valuta) {
                case "dolar":
                    result = newData.printDolar();
                    break;
                case "euro":
                    result = newData.printEuro();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
