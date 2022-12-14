package com.example.intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.intents.Constants.URL
import com.example.intents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var urlArl: ActivityResultLauncher<Intent>
    private lateinit var permissaoChamadaArl: ActivityResultLauncher<String>
    private lateinit var pegarImagemArl: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)
        supportActionBar?.subtitle = "MainActivity"


        urlArl = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ resultado ->
            if(resultado.resultCode == RESULT_OK ){
                val urlRetomada = resultado.data?.getStringExtra(URL) ?: ""
                amb.urlTv.text = urlRetomada
            }
        }

        //PEDIR PERMISSÃO AO USER PRA FAZER CHAMADA !!!
        permissaoChamadaArl = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            object: ActivityResultCallback<Boolean> {
                override fun onActivityResult(concedida: Boolean?) {
                    if(concedida !=null && concedida){
                        chamarNumero(true)
                    }
                    else{
                        Toast.makeText(this@MainActivity,
                            "Conceda permissão par a execução !",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        )


        pegarImagemArl = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ resultado ->
            if(resultado.resultCode == RESULT_OK ){
                val imagemUri = resultado.data?.data
                imagemUri?.let {
                    amb.urlTv.text = it.toString()
                }

                val visualizarImagemIntent = Intent(ACTION_VIEW, imagemUri)
                startActivity(visualizarImagemIntent)

            }
        }

        amb.entrarUrlBt.setOnClickListener {
            val urlActivityIntent = Intent(this, UrlActivity::class.java)

            urlActivityIntent.putExtra(URL, amb.urlTv.text.toString())
            urlArl.launch(urlActivityIntent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.viewMi -> {
                val url = Uri.parse(amb.urlTv.text.toString())
                val navegadorIntent = Intent(ACTION_VIEW, url)
                startActivity(navegadorIntent)
                true
            }
            R.id.dialMi -> {
                chamarNumero(false)
                true
            }
            R.id.callMi ->{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED){
                        chamarNumero(true)

                    }else{
                        permissaoChamadaArl.launch(CALL_PHONE)
                    }
                }
                else{
                    chamarNumero(true)
                }
                true
            }
            R.id.pickMi -> {
                val pegarImagemIntent = Intent(ACTION_PICK)
                val diretorioImagens = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                pegarImagemIntent.setDataAndType(Uri.parse(diretorioImagens), "image/*")
                startActivity(pegarImagemIntent)
                true
            }
            R.id.chooserMi -> {
                val escolherAppIntent = Intent(ACTION_CHOOSER)
                val informacoesIntent = Intent(ACTION_VIEW, Uri.parse(amb.urlTv.text.toString()))
                escolherAppIntent.putExtra(EXTRA_TITLE, "Escolha seu navegador")
                escolherAppIntent.putExtra(EXTRA_INTENT, informacoesIntent)

                startActivity(escolherAppIntent)
                true
            }

            else -> {false}
        }
    }

    private fun chamarNumero(chamar:Boolean){
        val uri = Uri.parse("tel: ${amb.urlTv.text}" )
        val intent = Intent(if (chamar) ACTION_CALL else ACTION_DIAL)
        intent.data = uri
        startActivity(intent)
    }


}