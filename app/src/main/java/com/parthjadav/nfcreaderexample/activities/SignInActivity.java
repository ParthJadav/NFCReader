package com.parthjadav.nfcreaderexample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parthjadav.nfcreaderexample.R;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener
{
	public static String LOG_TAG = "BlueSignIn";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		Button bSignIn = findViewById(R.id.bSignIn);
		TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
		TextView tvSignUp = findViewById(R.id.tvSignUp);

		bSignIn.setOnClickListener(this);
		tvForgotPassword.setOnClickListener(this);
		tvSignUp.setOnClickListener(this);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.bSignIn:
				startActivity(new Intent(SignInActivity.this, DashboardActivity.class));
				break;
			case R.id.tvForgotPassword:
				startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
				break;
			case R.id.tvSignUp:
				startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
				break;
		}
	}
}
