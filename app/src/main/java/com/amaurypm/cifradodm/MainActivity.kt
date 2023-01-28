package com.amaurypm.cifradodm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.amaurypm.cifradodm.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var  cryptoManager: CryptoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cryptoManager = CryptoManager()

        binding.btnEncrypt.setOnClickListener {
            val textToEncrypt = binding.tietText.text.toString()
            if(!textToEncrypt.isEmpty()) {
                val bytes = binding.tietText.text.toString().encodeToByteArray()
                //Usamos el almacenamiento interno, por lo que no se necesitan permisos
                val file = File(filesDir, "encrypted_data.txt")
                //Si el archivo no existe, lo creamos
                if (!file.exists()) {
                    file.createNewFile()
                }
                val fos = FileOutputStream(file)
                //Escribimos en el archivo creado, además de poner el resultado en el textview
                binding.tvDecrypt.text = cryptoManager.encrypt(
                    bytes,
                    fos
                ).decodeToString()
            }
            else{
                Toast.makeText(this, "Por favor ingresa un texto a encriptar", Toast.LENGTH_SHORT).show()
                binding.tietText.error = "No puede estar vacío"
                binding.tietText.requestFocus()
            }
        }

        binding.btnDecrypt.setOnClickListener {
            val file = File(filesDir, "encrypted_data.txt")
            if(file.exists()) {
                binding.tvDecrypt.text = cryptoManager.decrypt(
                    FileInputStream(file)
                ).decodeToString()
            }else{
                Toast.makeText(this, "No existe información a desencriptar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}