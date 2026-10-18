import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.southpoint_crms.R
import com.google.firebase.firestore.FirebaseFirestore

class StudentNotificationsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val currentStudentId = "current_user_id" // Replace with actual logged-in ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_notifications)

        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        // Listen for this student's notifications
        db.collection("notifications")
            .whereEqualTo("userId", currentStudentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val messages = mutableListOf<String>()
                    for (doc in snapshot.documents) {
                        val message = doc.getString("message") ?: ""
                        messages.add(message)
                    }

                    rvNotifications.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                            val tv = TextView(parent.context)
                            tv.setPadding(16, 16, 16, 16)
                            tv.textSize = 16f
                            return object : RecyclerView.ViewHolder(tv) {}
                        }
                        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                            (holder.itemView as TextView).text = "🔔 ${messages[position]}"
                        }
                        override fun getItemCount() = messages.size
                    }
                }
            }
    }
}