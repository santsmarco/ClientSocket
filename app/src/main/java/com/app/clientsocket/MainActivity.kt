package com.app.clientsocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.Scanner

class MainActivity : AppCompatActivity() {

    private lateinit var sendButton: Button
    private lateinit var edtXml: EditText
    private lateinit var edtIpServer: EditText
    private lateinit var txtResponseServer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendButton = findViewById(R.id.btn_send_xml)
        edtXml = findViewById(R.id.edt_xml)
        edtIpServer = findViewById(R.id.edt_ip_server)
        txtResponseServer = findViewById(R.id.txtResponseServer)
        sendButton.setOnClickListener {
            var ip = edtIpServer.text.toString()
            if (ip.isNotEmpty()) {
                val serverAddress = edtIpServer.text.toString() // Endereço IP do servidor
                val serverPort = "2222" // Porta do servidor
                val xmlData = getString(R.string.xml_example)
                sendDataToServer(xmlData, serverAddress, serverPort)
            } else {
                Toast.makeText(this, "Digite um ip", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendDataToServer(xmlData: String, serverAddress: String, serverPort: String) {
        Thread {
            var socket: Socket? = null
            try {
                socket = Socket()

                // Tempo limite para a conexão (em milissegundos)
                val connectionTimeout = 15000 // 15 segundos
                socket.connect(InetSocketAddress(serverAddress, serverPort.toInt()), connectionTimeout)

                socket.receiveBufferSize = 20000
                val outputStream: OutputStream = socket.getOutputStream()

                // Substituir "xmlData" pela sua string XML
                // val xmlData = edtXml.text.toString()

                // Enviar os dados para o servidor com o XML
                outputStream.write(xmlData.toByteArray())
                outputStream.flush()
                socket.shutdownOutput()

                socket.tcpNoDelay = true

                try {
                    // Obter resposta do servidor com confirmação do recebimento
                    val inputStream = socket.getInputStream()
                    val scanner = Scanner(inputStream)

                    val readTimeout = 15000 // 15 segundos
                    socket.soTimeout = readTimeout

                    val serverResponse = scanner.nextLine()

                    val jsonResponse = JSONObject(serverResponse)
                    val success = jsonResponse.getBoolean("success")
                    val errorMessage = jsonResponse.getString("errorMessage")

                    if (success) {
                        runOnUiThread {
                            txtResponseServer.text = "Response do servidor: ${serverResponse}"
                            Toast.makeText(this, "Sucesso envio", Toast.LENGTH_SHORT).show()
                        }
                        //fazer evaluateJavaScrip pro front
                    } else {
                        runOnUiThread {
                            txtResponseServer.text = "Response do servidor: ${serverResponse}"
                            Toast.makeText(this, "Erro envio", Toast.LENGTH_SHORT).show()
                        }
                        //fazer evaluateJavaScrip pro front
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        txtResponseServer.text = "Erro de E/S ao ler resposta do servidor: ${e.message.toString()}"
                        Toast.makeText(this, "Erro de E/S ao ler resposta do servidor", Toast.LENGTH_SHORT).show()
                    }
                    //fazer evaluateJavaScrip pro front
                } catch (e: SocketTimeoutException) {
                    runOnUiThread {
                        txtResponseServer.text = "Tempo limite excedido ao ler resposta do servidor: ${e.message.toString()}"
                        Toast.makeText(this, "Tempo limite excedido ao ler resposta do servidor", Toast.LENGTH_SHORT).show()
                        //fazer evaluateJavaScrip pro front com false após timeout
                    }
                } catch (e: NullPointerException) {
                    runOnUiThread {
                        txtResponseServer.text = "NullPointerException ao ler resposta do servidor: ${e.message.toString()}"
                        Toast.makeText(this, "NullPointerException ao ler resposta do servidor", Toast.LENGTH_SHORT).show()
                    }
                    //fazer evaluateJavaScrip pro front
                }
            } catch (e: IOException) {
                runOnUiThread {
                    txtResponseServer.text = "Erro de E/S durante a conexão: ${e.message.toString()}"
                    Toast.makeText(this, "Erro de E/S durante a conexão", Toast.LENGTH_SHORT).show()
                }
                //fazer evaluateJavaScrip pro front
            } catch (e: SocketTimeoutException) {
                runOnUiThread {
                    txtResponseServer.text = "Tempo limite excedido ao enviar resposta ao servidor: ${e.message.toString()}"
                    Toast.makeText(this, "Tempo limite excedido ao enviar resposta ao servidor", Toast.LENGTH_SHORT).show()
                    //fazer evaluateJavaScrip pro front com false após timeout
                }
            } catch (e: Exception) {
                runOnUiThread {
                    txtResponseServer.text = "Exception: ${e.message.toString()}"
                    Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show()
                }
                //fazer evaluateJavaScrip pro front
            } finally {
                try {
                    socket?.close()
                } catch (e: IOException) {
                    txtResponseServer.text = "IOException ao fechar a conexão: ${e.message.toString()}"
                    Toast.makeText(this, "Falha ao fechar a conexão", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}