package com.example.albaayo.option;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.albaayo.EmployerCompanyMain;
import com.example.albaayo.EmployerMainPage;
import com.example.albaayo.R;
import com.example.albaayo.employer.UpdateCompany;
import com.example.http.Http;
import com.example.http.dto.Id;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployerGroupOption extends AppCompatActivity {

    private Long companyId;
    private String companyName;
    private Button updateButton, workContractButton, groupDeleteButton;
    private TextView headerText;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employer_group_page);

        initData();

        groupDelete();

        updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(EmployerGroupOption.this, UpdateCompany.class);
            intent.putExtra("companyId", companyId);
            startActivity(intent);
        });
    }

    private void groupDelete() {
        groupDeleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(EmployerGroupOption.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage("그룹을 삭제하시겠습니까?")
                    .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(DialogInterface dialog, int which){
                            Call<Void> call = Http.getInstance().getApiService()
                                    .removeCompany(Id.getInstance().getAccessToken(), companyId);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.code() == 401) {
                                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                        editor.putString("accessToken", response.headers().get("Authorization"));
                                        editor.commit();

                                        Call<Void> reCall = Http.getInstance().getApiService()
                                                .removeCompany(Id.getInstance().getAccessToken(), companyId);
                                        reCall.enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                Intent intent = new Intent(EmployerGroupOption.this, EmployerMainPage.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {

                                            }
                                        });
                                    } else {
                                        Intent intent = new Intent(EmployerGroupOption.this, EmployerMainPage.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {

                                }
                            });
                        }
                    }).setPositiveButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
        });
    }

    private void initData() {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");

        headerText = findViewById(R.id.header_name_text);
        headerText.setText(companyName);

        updateButton = findViewById(R.id.update_info);
        workContractButton = findViewById(R.id.work_contract);
        groupDeleteButton = findViewById(R.id.group_delete);
    }
}
