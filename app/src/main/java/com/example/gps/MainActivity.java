package com.example.gps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private Button btnIrParaMapa;
    private EditText editNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnIrParaMapa = findViewById(R.id.btnIrParaMapa);
        editNome = findViewById(R.id.editNome);

        btnIrParaMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ValidaCampo()){
                    IrParaMapa();
                }
            }
        });
    }

    public boolean ValidaCampo(){
        if(editNome.length()==0){
            Toast.makeText(this, "Preencha o campo!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }
    }

    public String LerNome(){
        return editNome.getText().toString();
    }

    public void IrParaMapa(){

        String MeuNome = LerNome();

        Intent intent = new Intent(MainActivity.this, gnss_view.class);
        intent.putExtra("nome", MeuNome);
        startActivity(intent);
    }

}