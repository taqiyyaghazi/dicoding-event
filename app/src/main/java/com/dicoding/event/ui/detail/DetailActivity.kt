package com.dicoding.event.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.event.R
import com.dicoding.event.data.response.Event
import com.dicoding.event.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var detailViewModel: DetailViewModel
    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val TAG = "DETAIL_ACTIVITY"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, 0)

        detailViewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        detailViewModel.fetchDetailEvent(eventId)

        detailViewModel.event.observe(this) { event ->
            showDetailEvent(event)
        }

        detailViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        detailViewModel.errorMessage.observe(this) { errorMsg ->
            errorMsg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showDetailEvent(event: Event) {
        binding.eventName.text = event.name
        binding.eventSummary.text = event.summary
        binding.eventOwner.text = event.ownerName
        binding.eventTime.text = event.beginTime
        binding.eventKuota.text =
            getString(R.string.available_quota, (event.quota - event.registrants).toString())
        binding.eventDescription.text = event.description.let {
            HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } ?: ""
        Glide.with(this)
            .load(event.mediaCover)
            .into(binding.eventCover)

        binding.actionOpen.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(event.link)
            startActivity(intent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}