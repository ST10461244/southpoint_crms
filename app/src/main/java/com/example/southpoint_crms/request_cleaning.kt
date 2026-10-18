import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.southpoint_crms.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RequestCleaningActivity : AppCompatActivity() {

    private lateinit var rgBookingFor: RadioGroup
    private lateinit var rbMyself: RadioButton
    private lateinit var rbSomeoneElse: RadioButton
    private lateinit var tilRoomNumber: View
    private lateinit var tilExternalId: View
    private lateinit var etRoomNumber: EditText
    private lateinit var etExternalId: EditText
    private lateinit var etDate: EditText
    private lateinit var etInstructions: EditText
    private lateinit var btnSubmit: Button

    private val db = FirebaseFirestore.getInstance()
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_cleaning)

        // Bind UI elements
        rgBookingFor = findViewById(R.id.rgBookingFor)
        rbMyself = findViewById(R.id.rbMyself)
        rbSomeoneElse = findViewById(R.id.rbSomeoneElse)
        tilRoomNumber = findViewById(R.id.tilRoomNumber)
        tilExternalId = findViewById(R.id.tilExternalId)
        etRoomNumber = findViewById(R.id.etRoomNumber)
        etExternalId = findViewById(R.id.etExternalId)
        etDate = findViewById(R.id.etDate)
        etInstructions = findViewById(R.id.etInstructions)
        btnSubmit = findViewById(R.id.btnSubmitRequest)

        // Setup Time Slot Spinner
        val spinner = findViewById<Spinner>(R.id.spinnerTimeSlot)
        val timeSlots = arrayOf("09:00 AM - 11:00 AM", "12:00 PM - 02:00 PM", "03:00 PM - 05:00 PM")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeSlots)

        // Radio Button Logic (Myself vs Someone Else)
        rgBookingFor.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbMyself) {
                tilRoomNumber.visibility = View.GONE
                tilExternalId.visibility = View.GONE
                // TODO: Auto-fill room number from the logged-in student's profile data
            } else {
                tilRoomNumber.visibility = View.VISIBLE
                tilExternalId.visibility = View.VISIBLE
            }
        }

        // Date Picker Logic
        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                selectedDate = "$d/${m + 1}/$y"
                etDate.setText(selectedDate)
            }, year, month, day)

            // Prevent past dates
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        // Submit Button Logic
        btnSubmit.setOnClickListener {
            submitBooking(spinner.selectedItem.toString())
        }
    }

    private fun submitBooking(selectedTimeSlot: String) {
        val isMyself = rbMyself.isChecked
        val roomNumber = if (isMyself) "AUTO_FILLED_ROOM" else etRoomNumber.text.toString()
        val instructions = etInstructions.text.toString()

        // Basic Validation
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isMyself && (roomNumber.isEmpty() || etExternalId.text.toString().isEmpty())) {
            Toast.makeText(this, "Please fill in Room Number and External ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent double clicks
        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."

        // Create the booking object matching your Firestore schema
        val newBooking = hashMapOf(
            "studentId" to "current_user_id", // TODO: Replace with Firebase Auth UID
            "studentName" to "John Doe",      // TODO: Replace with logged-in user name
            "roomNumber" to roomNumber,
            "date" to selectedDate,
            "timeSlot" to selectedTimeSlot,
            "specialInstructions" to instructions,
            "status" to "Pending",
            "passkey" to (100000..999999).random().toString(), // Generate passkey immediately
            "timestamp" to System.currentTimeMillis()
        )

        // Save to Firestore
        db.collection("bookings")
            .add(newBooking)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Booking Requested! ID: ${documentReference.id}", Toast.LENGTH_LONG).show()
                finish() // Go back to home screen
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Request"
            }
    }
}