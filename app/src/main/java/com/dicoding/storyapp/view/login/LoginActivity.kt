package com.dicoding.storyapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.view.main.MainActivity
import com.dicoding.storyapp.view.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        loginViewModel = setViewModel(this)
        setAction()
        playAnimation()

        binding.tvToSignup.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun setViewModel(activity: AppCompatActivity): LoginViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[LoginViewModel::class.java]
    }

    private fun setAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    Toast.makeText(this, getString(R.string.null_email), Toast.LENGTH_SHORT).show()
                    binding.edLoginEmail.apply {
                        error = getString(R.string.null_email)
                        requestFocus()
                    }
                }

                if (password.isEmpty()) {
                    Toast.makeText(this, getString(R.string.null_password), Toast.LENGTH_SHORT).show()
                    binding.edLoginPassword.apply {
                        error = getString(R.string.null_password)
                        requestFocus()
                    }
                }

                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                binding.edLoginEmail.requestFocus()
                return@setOnClickListener
            } else if (password.length < 8) {
                Toast.makeText(this, getString(R.string.minimal_characters), Toast.LENGTH_SHORT).show()
                binding.edLoginPassword.requestFocus()
                return@setOnClickListener
            } else {
                loginViewModel.login(email, password)
                loginViewModel.user.observe(this) { user ->
                    Log.d(TAG, "${user.token} || ${user.userId} || ${user.name}")

                    Toast.makeText(this@LoginActivity, getString(R.string.success_login, user.name), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                loginViewModel.isLoading.observe(this) {
                    showLoading(it)
                }
                loginViewModel.error.observe(this) {
                    if (it)
                        Toast.makeText(this@LoginActivity, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 1f).setDuration(500)
        val pass = ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 1f).setDuration(500)
        val toSignUp = ObjectAnimator.ofFloat(binding.tvToSignup, View.ALPHA, 1f).setDuration(500)
        val submit = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title, email, pass, toSignUp, submit)
            startDelay = 300
        }.start()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}