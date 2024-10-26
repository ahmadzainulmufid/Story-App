package com.example.storyapp.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.core.util.Pair
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.result.Result
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.view.StoryAdapter
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.detail.DetailActivity
import com.example.storyapp.view.upload.UploadActivity
import com.example.storyapp.view.welcome.WelcomeActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Story Apps"
        }

        setupView()
        setupRecyclerView()
        setupObservers()
        setupAction()

        onBackPressedDispatcher.addCallback(this) {
            showExitConfirmationDialog()
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        viewModel.getStories().observe(this) { stories ->
            when (stories) {
                is Result.Error -> showSnackBar(stories.error)
                Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    storyAdapter.submitList(stories.data)
                }
            }
        }

    }

    private fun showSnackBar(message: String) {
        showLoading(false)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.getStories()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            supportActionBar?.show()
        }
    }

    private fun setupRecyclerView() {
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        storyAdapter = StoryAdapter { story, ivItemPhoto, tvItemName, tvItemDescription ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("EXTRA_STORY_ID", story.id)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair(ivItemPhoto, "shared_image"),
                Pair(tvItemName, "shared_name"),
                Pair(tvItemDescription, "shared_description")
            )
            startActivity(intent, options.toBundle())
        }
        binding.rvStories.adapter = storyAdapter
    }

    private fun setupObservers() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        viewModel.getStories().observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val stories = result.data
                    storyAdapter.submitList(stories)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupAction() {
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.logout)
        builder.setMessage(R.string.logout_confirmation)
        builder.setPositiveButton(R.string.ya) { _, _ ->
            viewModel.logout()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.tidak) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.exit)
        builder.setMessage(R.string.logout_confirmation)
        builder.setPositiveButton(R.string.ya) { _, _ ->
            finish()
        }
        builder.setNegativeButton(R.string.tidak) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
