import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.southpoint_crms.R
import com.google.firebase.firestore.FirebaseFirestore

class CleanerBookingsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val currentCleanerId = "cleaner_123" // Replace with actual logged-in ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaner_bookings)

        findViewById<View>(R.id.btnBackCleaner).setOnClickListener { finish() }

        val rvInProgress = findViewById<RecyclerView>(R.id.rvCleanerInProgress)
        val rvCompleted = findViewById<RecyclerView>(R.id.rvCleanerCompleted)

        rvInProgress.layoutManager = LinearLayoutManager(this)
        rvCompleted.layoutManager = LinearLayoutManager(this)

        // Listen for this cleaner's bookings
        db.collection("bookings")
            .whereEqualTo("cleanerId", currentCleanerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val inProgressList = mutableListOf<Map<String, Any>>()
                    val completedList = mutableListOf<Map<String, Any>>()

                    for (doc in snapshot.documents) {
                        val data = doc.data
                        if (data != null) {
                            data["docId"] = doc.id
                            if (data["status"] == "Completed") {
                                completedList.add(data)
                            } else {
                                inProgressList.add(data)
                            }
                        }
                    }

                    rvInProgress.adapter = CleanerBookingAdapter(inProgressList, isCompleted = false)
                    rvCompleted.adapter = CleanerBookingAdapter(completedList, isCompleted = true)
                }
            }
    }

    inner class CleanerBookingAdapter(
        private val bookings: List<Map<String, Any>>,
        private val isCompleted: Boolean
    ) : RecyclerView.Adapter<CleanerBookingAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDetails: TextView = view.findViewById(R.id.tvCleanerBookingDetails)
            val btnComplete: Button = view.findViewById(R.id.btnCompleteJob)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cleaner_booking, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val booking = bookings[position]
            holder.tvDetails.text = "Room: ${booking["roomNumber"]}\nDate: ${booking["date"]}\nStatus: ${booking["status"]}"

            if (isCompleted) {
                holder.btnComplete.visibility = View.GONE
            } else {
                holder.btnComplete.visibility = View.VISIBLE
                holder.btnComplete.setOnClickListener {
                    showPasskeyDialog(booking)
                }
            }
        }

        override fun getItemCount() = bookings.size
    }

    private fun showPasskeyDialog(booking: Map<String, Any>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Passkey")
        builder.setMessage("Ask the student for their 6-digit passkey to complete this job.")

        val input = EditText(this)
        input.hint = "e.g., 123456"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Verify") { _, _ ->
            val enteredPasskey = input.text.toString()
            val correctPasskey = booking["passkey"].toString()
            val bookingId = booking["docId"].toString()

            if (enteredPasskey == correctPasskey) {
                completeBookingInFirebase(bookingId, booking["studentId"].toString())
            } else {
                Toast.makeText(this, "Incorrect Passkey! Please try again.", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun completeBookingInFirebase(bookingId: String, studentId: String) {
        db.collection("bookings").document(bookingId)
            .update("status", "Completed")
            .addOnSuccessListener {
                // Create a notification for the student
                val notification = hashMapOf(
                    "userId" to studentId,
                    "message" to "Your cleaning is complete! Passkey verified.",
                    "bookingId" to bookingId,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("notifications").add(notification)

                Toast.makeText(this, "Job Completed Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error updating database", Toast.LENGTH_SHORT).show()
            }
    }
}