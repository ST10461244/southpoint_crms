import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.southpoint_crms.R
import com.google.firebase.firestore.FirebaseFirestore

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var rvInProgress: RecyclerView
    private lateinit var rvCompleted: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        rvInProgress = findViewById(R.id.rvInProgress)
        rvCompleted = findViewById(R.id.rvCompleted)

        rvInProgress.layoutManager = LinearLayoutManager(this)
        rvCompleted.layoutManager = LinearLayoutManager(this)

        // In a real app, this would be the logged-in user's ID
        val currentStudentId = "current_user_id"

        // Real-time listener
        db.collection("bookings")
            .whereEqualTo("studentId", currentStudentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val inProgressList = mutableListOf<String>()
                    val completedList = mutableListOf<String>()

                    for (doc in snapshot.documents) {
                        val status = doc.getString("status") ?: ""
                        val room = doc.getString("roomNumber") ?: ""
                        val date = doc.getString("date") ?: ""
                        val cleaner = doc.getString("cleanerName") ?: "Unassigned"

                        val displayString = "$date | $room | Cleaner: $cleaner"

                        if (status == "Completed") {
                            completedList.add(displayString)
                        } else if (status == "In Progress" || status == "Pending") {
                            inProgressList.add(displayString)
                        }
                    }

                    // For simplicity, we can use a basic ArrayAdapter for now
                    // (Or a custom RecyclerView.Adapter for better UI)
                    updateRecyclerView(rvInProgress, inProgressList)
                    updateRecyclerView(rvCompleted, completedList)
                }
            }
    }

    private fun updateRecyclerView(recyclerView: RecyclerView, items: List<String>) {
        // Quick adapter replacement to show text items
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context)
                tv.setPadding(16, 16, 16, 16)
                tv.setBackgroundColor(android.graphics.Color.WHITE)
                return object : RecyclerView.ViewHolder(tv) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).text = items[position]
            }
            override fun getItemCount() = items.size
        }
        recyclerView.adapter = adapter
    }
}