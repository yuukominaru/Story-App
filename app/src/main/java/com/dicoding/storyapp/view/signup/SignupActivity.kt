package com.dicoding.storyapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivitySignupBinding
import com.dicoding.storyapp.view.login.LoginActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel by viewModels<SignupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setAction()
        playAnimation()

        binding.tvToLogin.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun setAction() {
        binding.btnSignup.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                if (name.isEmpty()) {
                    Toast.makeText(this, getString(R.string.null_name), Toast.LENGTH_SHORT).show()
                    binding.edRegisterName.apply {
                        error = getString(R.string.null_name)
                        requestFocus()
                    }
                }

                if (email.isEmpty()) {
                    Toast.makeText(this, resources.getString(R.string.null_email), Toast.LENGTH_SHORT).show()
                    binding.edRegisterEmail.apply {
                        error = getString(R.string.null_email)
                        requestFocus()
                    }
                }

                if (password.isEmpty()) {
                    Toast.makeText(this, resources.getString(R.string.null_password), Toast.LENGTH_SHORT).show()
                    binding.edRegisterPassword.apply {
                        error = getString(R.string.null_password)
                        requestFocus()
                    }
                }

                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, resources.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                binding.edRegisterEmail.requestFocus()
                return@setOnClickListener
            } else if (password.length < 8) {
                Toast.makeText(this, resources.getString(R.string.minimal_characters), Toast.LENGTH_SHORT).show()
                binding.edRegisterPassword.requestFocus()
                return@setOnClickListener
            } else {
                signupViewModel.register(name, email, password)
                signupViewModel.result.observe(this) { result ->
                    if (!result.error) {
                        Toast.makeText(this@SignupActivity, resources.getString(R.string.success_signup), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else
                        Toast.makeText(this@SignupActivity, result.message, Toast.LENGTH_SHORT).show()
                }
                signupViewModel.isLoading.observe(this) {
                    showLoading(it)
                }
                signupViewModel.error.observe(this) {
                    if (!it)
                        Toast.makeText(this@SignupActivity, "Register Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.tvSignup, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 1f).setDuration(500)
        val pass = ObjectAnimator.ofFloat(binding.edRegisterPassword, View.ALPHA, 1f).setDuration(500)
        val toLogin = ObjectAnimator.ofFloat(binding.tvToLogin, View.ALPHA, 1f).setDuration(500)
        val submit = ObjectAnimator.ofFloat(binding.btnSignup, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title, name, email, pass, toLogin, submit)
            startDelay = 300
        }.start()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
