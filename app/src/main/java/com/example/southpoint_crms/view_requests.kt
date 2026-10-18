import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.southpoint_crms.R
import com.google.firebase.firestore.FirebaseFirestore

class ViewRequestsActivity : AppCompatActivity() {

    private lateinit var rvRequests: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val requestList = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: RequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_requests)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        rvRequests = findViewById(R.id.rvAvailableRequests)
        rvRequests.layoutManager = LinearLayoutManager(this)
        adapter = RequestAdapter(requestList)
        rvRequests.adapter = adapter

        // Listen for pending requests in real-time
        db.collection("bookings")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    requestList.clear()
                    for (doc in snapshot.documents) {
                        val data = doc.data
                        if (data != null) {
                            data["docId"] = doc.id // Store the document ID to accept it later
                            requestList.add(data)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    // Inner Adapter Class for the RecyclerView
    inner class RequestAdapter(private val requests: List<Map<String, Any>>) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRoom: TextView = view.findViewById(R.id.tvNumber)
            val tvDate: TextView = view.findViewById(R.id.tvDateTime)
            val tvInstructions: TextView = view.findViewById(R.id.tvInstructions)
            val btnAccept: Button = view.findViewById(R.id.btnAccept)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request_card, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val request = requests[position]
            holder.tvRoom.text = "Room: ${request["roomNumber"]}"
            holder.tvDate.text = "${request["date"]} | ${request["timeSlot"]}"
            holder.tvInstructions.text = request["specialInstructions"].toString().ifEmpty { "No special instructions" }

            holder.btnAccept.setOnClickListener {
                val docId = request["docId"].toString()
                acceptRequest(docId)
            }
        }

        override fun getItemCount() = requests.size
    }

    private fun acceptRequest(bookingId: String) {
        // In a real app, get the actual cleaner ID and Name from auth/session
        val cleanerId = "cleaner_123"
        val cleanerName = "Cleaner John"

        db.collection("bookings").document(bookingId)
            .update(
                "status", "In Progress",
                "cleanerId", cleanerId,
                "cleanerName", cleanerName
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Job Accepted!", Toast.LENGTH_SHORT).show()
                // The snapshot listener will automatically remove this item from the list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to accept job", Toast.LENGTH_SHORT).show()
            }
    }
}