package com.wayfarer.app.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.wayfarer.app.R
import com.wayfarer.app.data.models.BookingRequest
import com.wayfarer.app.data.models.Hotel
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.databinding.BottomSheetBookingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBookingBinding? = null
    private val binding get() = _binding!!

    private var tourId = ""
    private var tourName = ""
    private var tourPrice = 0.0
    private var tourDuration = 0

    private var currentStep = 1
    private val totalSteps = 4

    private var checkInDate = 0L
    private var checkOutDate = 0L
    private var nights = 0
    private var selectedHotel: Hotel? = null
    private var rooms = 1
    private var guests = 1
    private var tripType = "Solo"
    private var paymentMethod = "Card"

    private lateinit var hotelAdapter: HotelBookingAdapter

    var onBookingConfirmed: ((BookingRequest) -> Unit)? = null

    companion object {
        private val DATE_FMT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun newInstance(
            tourId: String, tourName: String,
            tourPrice: Double, tourDuration: Int
        ) = BookingBottomSheet().apply {
            arguments = Bundle().apply {
                putString("tourId", tourId)
                putString("tourName", tourName)
                putDouble("tourPrice", tourPrice)
                putInt("tourDuration", tourDuration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tourId = it.getString("tourId", "")
            tourName = it.getString("tourName", "")
            tourPrice = it.getDouble("tourPrice", 0.0)
            tourDuration = it.getInt("tourDuration", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isFitToContents = false
        }

        setupHotels()
        setupRoomControls()
        setupGuestControls()
        setupTripTypeChips()
        setupPaymentOptions()
        setupNavButtons()

        binding.btnPickDates.setOnClickListener { showDatePicker() }

        showStep(1)
    }

    private fun setupHotels() {
        hotelAdapter = HotelBookingAdapter { hotel ->
            selectedHotel = hotel
            hotelAdapter.setSelected(hotel.id)
        }
        binding.rvHotels.adapter = hotelAdapter
        binding.rvHotels.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        hotelAdapter.submitList(WayFarerRepository.getHotelsForTour(tourId))
    }

    private fun setupRoomControls() {
        updateRoomDisplay()
        binding.btnRoomMinus.setOnClickListener {
            if (rooms > 1) { rooms--; updateRoomDisplay() }
        }
        binding.btnRoomPlus.setOnClickListener {
            if (rooms < 10) { rooms++; updateRoomDisplay() }
        }
    }

    private fun updateRoomDisplay() {
        binding.tvRoomCount.text = rooms.toString()
    }

    private fun setupGuestControls() {
        updateGuestDisplay()
        binding.btnGuestMinus.setOnClickListener {
            if (guests > 1) { guests--; updateGuestDisplay() }
        }
        binding.btnGuestPlus.setOnClickListener {
            if (guests < 20) { guests++; updateGuestDisplay() }
        }
    }

    private fun updateGuestDisplay() {
        binding.tvGuestCount.text = guests.toString()
    }

    private fun setupTripTypeChips() {
        binding.chipGroupTrip.setOnCheckedStateChangeListener { _, checkedIds ->
            tripType = when (checkedIds.firstOrNull()) {
                R.id.chip_couple -> "Couple"
                R.id.chip_family -> "Family"
                R.id.chip_trip_group -> "Group"
                else -> "Solo"
            }
        }
    }

    private fun setupPaymentOptions() {
        binding.cardPayCard.setOnClickListener { selectPayment("Card") }
        binding.cardPayUpi.setOnClickListener { selectPayment("UPI") }
        binding.cardPayLater.setOnClickListener { selectPayment("Pay Later") }
        updatePaymentUI()
    }

    private fun selectPayment(method: String) {
        paymentMethod = method
        updatePaymentUI()
    }

    private fun updatePaymentUI() {
        val active = ContextCompat.getColor(requireContext(), R.color.dark_navy)
        val inactive = ContextCompat.getColor(requireContext(), R.color.divider)

        fun apply(isSelected: Boolean, checkView: View, card: com.google.android.material.card.MaterialCardView) {
            checkView.visibility = if (isSelected) View.VISIBLE else View.GONE
            card.strokeColor = if (isSelected) active else inactive
            card.strokeWidth = if (isSelected) 3 else 1
        }

        apply(paymentMethod == "Card", binding.icCheckCard, binding.cardPayCard)
        apply(paymentMethod == "UPI", binding.icCheckUpi, binding.cardPayUpi)
        apply(paymentMethod == "Pay Later", binding.icCheckLater, binding.cardPayLater)
    }

    private fun setupNavButtons() {
        binding.btnBack.setOnClickListener {
            if (currentStep > 1) showStep(currentStep - 1) else dismiss()
        }
        binding.btnContinue.setOnClickListener { handleContinue() }
    }

    private fun handleContinue() {
        when (currentStep) {
            1 -> {
                if (checkInDate == 0L || checkOutDate == 0L) {
                    Toast.makeText(requireContext(), "Please select your travel dates", Toast.LENGTH_SHORT).show()
                    return
                }
                showStep(2)
            }
            2 -> {
                if (selectedHotel == null) {
                    Toast.makeText(requireContext(), "Please select a hotel", Toast.LENGTH_SHORT).show()
                    return
                }
                showStep(3)
            }
            3 -> showStep(4)
            4 -> confirmBooking()
        }
    }

    private fun showStep(step: Int) {
        currentStep = step
        binding.stepDates.visibility = if (step == 1) View.VISIBLE else View.GONE
        binding.stepHotel.visibility = if (step == 2) View.VISIBLE else View.GONE
        binding.stepGuests.visibility = if (step == 3) View.VISIBLE else View.GONE
        binding.stepPayment.visibility = if (step == 4) View.VISIBLE else View.GONE

        binding.tvStepCount.text = "Step $step of $totalSteps"
        binding.progressSteps.progress = (step * 100) / totalSteps

        when (step) {
            1 -> {
                binding.tvStepTitle.text = "Travel Dates"
                binding.btnBack.text = "Cancel"
                binding.btnContinue.text = "Continue"
            }
            2 -> {
                binding.tvStepTitle.text = "Choose Hotel"
                binding.btnBack.text = "Back"
                binding.btnContinue.text = "Continue"
            }
            3 -> {
                binding.tvStepTitle.text = "Travellers"
                binding.btnBack.text = "Back"
                binding.btnContinue.text = "Continue"
            }
            4 -> {
                binding.tvStepTitle.text = "Payment"
                binding.btnBack.text = "Back"
                binding.btnContinue.text = "Confirm & Pay"
                populateSummary()
            }
        }
    }

    private fun populateSummary() {
        val hotel = selectedHotel ?: return
        val hotelCost = hotel.pricePerNight * nights * rooms
        val total = tourPrice + hotelCost

        binding.tvSummaryDates.text =
            "📅  ${DATE_FMT.format(Date(checkInDate))} → ${DATE_FMT.format(Date(checkOutDate))}  ($nights nights)"
        binding.tvSummaryHotelName.text = "🏨  ${hotel.name}"
        binding.tvSummaryGuests.text =
            "👥  $guests guest${if (guests > 1) "s" else ""}  •  $rooms room${if (rooms > 1) "s" else ""}  •  $tripType"
        binding.tvSummaryTour.text = "Tour price:  $${tourPrice.toInt()}"
        binding.tvSummaryHotel.text =
            "Hotel ($rooms room${if (rooms > 1) "s" else ""} × $nights nights × $${hotel.pricePerNight.toInt()}):  $${hotelCost.toInt()}"
        binding.tvSummaryTotal.text = "Grand Total:  $${total.toInt()}"
    }

    private fun confirmBooking() {
        val hotel = selectedHotel ?: return
        val total = tourPrice + hotel.pricePerNight * nights * rooms
        onBookingConfirmed?.invoke(
            BookingRequest(
                tourId = tourId,
                tourPrice = tourPrice,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                hotelId = hotel.id,
                hotelName = hotel.name,
                hotelPricePerNight = hotel.pricePerNight,
                nights = nights,
                rooms = rooms,
                guests = guests,
                tripType = tripType,
                paymentMethod = paymentMethod,
                totalAmount = total
            )
        )
        dismiss()
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Travel Dates")
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            checkInDate = selection.first ?: 0L
            checkOutDate = selection.second ?: 0L
            nights = ((checkOutDate - checkInDate) / (1000L * 60 * 60 * 24))
                .toInt().coerceAtLeast(1)
            binding.tvCheckIn.text = DATE_FMT.format(Date(checkInDate))
            binding.tvCheckOut.text = DATE_FMT.format(Date(checkOutDate))
            binding.tvNights.text = "$nights night${if (nights != 1) "s" else ""}"
        }

        picker.show(parentFragmentManager, "date_range_picker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
