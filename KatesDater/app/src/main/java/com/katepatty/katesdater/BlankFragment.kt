package com.katepatty.katesdater

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.katepatty.katesdater.Dao
import com.katepatty.katesdater.DB

import kotlinx.coroutines.launch

import android.app.Application
import android.util.Log
import android.widget.AutoCompleteTextView
import androidx.lifecycle.*


// for VM factory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


// data bind

import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.katepatty.katesdater.databinding.FragmentBlankBinding

// rating
import android.widget.RatingBar
//import kotlinx.android.synthetic.main.fragment_blank.*


class BlankFragment : Fragment(),RatingBar.OnRatingBarChangeListener {

    var i: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentBlankBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_blank, container, false)

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        val dataSource = DB.getInstance(application).dao
        //val arguments = BlankFragmentArgs.fromBundle(requireArguments())
        //val arguments = BlankFragmentArgs.fromBundle(arguments!!)
        //val viewModelFactory = KViewModelFactory(dataSource, application, arguments.key)
        val viewModelFactory = KViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        val kvm =
            ViewModelProvider(
                this, viewModelFactory).get(KViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.kvm = kvm

        //binding.textView.text = binding.ratingBar.rating.toFloat().toString()

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating,
                                                         fromUser -> binding.textView.text = "Service Rating :" + rating.toString() }


        //var i = binding.ratingBar.rating.toDouble()
        //convertRateToString(i)


        //Insert Case
        /*val thread = Thread {
            var systemTime: Long = 0L
            var tt = Touring()
            tt.id = systemTime
            tt.foodQuality = binding.ratingBar.rating.toDouble()

            db.bookDao().save(tt)

            //fetch Records
            db.dao().getAll().forEach()
            {
                Log.i("Fetch Records", "Id:  : ${it.id}")
                Log.i("Fetch Records", "Name:  : ${it.name}")
            }
        }
        thread.start()*/




        //binding.ratingBar.onRatingBarChangeListener = this

        //ratingBar.onRatingBarChangeListener = this


        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)


        // Add an Observer on the state variable for showing a Snackbar message
        // when the CLEAR button is pressed.
        kvm.showSnackBarEvent.observe(this, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                kvm.doneShowingSnackbar()
            }
        })



        // Add an Observer on the state variable for Navigating when STOP button is pressed.
        /*kvm.navigateToQuality.observe(this, Observer { tour ->
            tour?.let {
                // We need to get the navController from this, because button is not ready, and it
                // just has to be a view. For some reason, this only matters if we hit stop again
                // after using the back button, not if we hit stop and choose a quality.
                // Also, in the Navigation Editor, for Quality -> Tracker, check "Inclusive" for
                // popping the stack to get the correct behavior if we press stop multiple times
                // followed by back.
                // Also: https://stackoverflow.com/questions/28929637/difference-and-uses-of-oncreate-oncreateview-and-onactivitycreated-in-fra
                this.findNavController().navigate(
                    BlankFragmentDirections
                        .actionSleepTrackerFragmentToSleepQualityFragment(tour.id))
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                kvm.doneNavigating()
            }
        })*/



        return binding.root
    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {

    }



}


//() -> QualityViewModel.onSetSleepQuality(0.1)
class QualityViewModel(
    private val key: Long = 0L, rate: Double,
    val dao: Dao) : ViewModel() {

        fun onSetQuality(rate: Double){

            viewModelScope.launch {
                var systemTime: Long = 0L
                var tt = Touring()
                tt.id = systemTime
                tt.foodQuality = rate
                dao.update(tt)
            }

        }

    }



/**
 * ViewModel for SleepTrackerFragment.
 */
class KViewModel(

    val dao: Dao,
    application: Application) : AndroidViewModel(application) {

    private var tour = MutableLiveData<Touring?>()

    private val tours = dao.getAllTours()

    /**
     * Converted tours to Spanned for displaying.
     */
    val tourString = Transformations.map(tours) { tours ->
        formater(tours, application.resources)
    }

    /**
     * If tonight has not been set, then the START button should be visible.
     */
    val startButtonVisible = Transformations.map(tour) {
        null == it
    }

    /**
     * If tonight has been set, then the STOP button should be visible.
     */
    val stopButtonVisible = Transformations.map(tour) {
        null != it
    }

    /**
     * If there are any nights in the database, show the CLEAR button.
     */
    val clearButtonVisible = Transformations.map(tours) {
        it?.isNotEmpty()
    }


    /**
     * Request a toast by setting this value to true.
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    /**
     * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
     */
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    /**
     * Variable that tells the Fragment to navigate to a specific [QualityFragment]
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    //private val _navigateToQuality = MutableLiveData<Touring>()

    /**
     * If this is non-null, immediately navigate to [QualityFragment] and call [doneNavigating]
     */
   // val navigateToQuality: LiveData<Touring>
        //get() = _navigateToQuality

    /**
     * Call this immediately after calling `show()` on a toast.
     *
     * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
     * toast.
     */
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    /**
     * Call this immediately after navigating to [SleepQualityFragment]
     *
     * It will clear the navigation request, so if the user rotates their phone it won't navigate
     * twice.
     */
    //fun doneNavigating() {
       // _navigateToQuality.value = null
    //}

    init {
        initializeTour()
    }

    private fun initializeTour() {
        viewModelScope.launch {
            tour.value = getTourFromDatabase()
        }
    }

    /**
     *  Handling the case of the stopped app or forgotten recording,
     *  the start and end times will be the same.j
     *
     *  If the start time and end time are not the same, then we do not have an unfinished
     *  recording.
     */
    private suspend fun getTourFromDatabase(): Touring? {
        var t = dao.getTour()
        if (t?.endTimeMilli != t?.startTimeMilli) {
            t = null
        }
        return t
    }

    private suspend fun insert(t: Touring) {
        dao.insert(t)
    }

    private suspend fun update(t: Touring) {
        dao.update(t)
    }

    private suspend fun clear() {
        dao.clear()
    }

    /**
     * Executes when the START button is clicked.
     */
    fun onStartTracking() {
        viewModelScope.launch {
            // Create a new night, which captures the current time,
            // and insert it into the database.
            val newNight = Touring()

            insert(newNight)

            tour.value = getTourFromDatabase()
        }
    }

    /**
     * Executes when the STOP button is clicked.
     */
    fun onStopTracking() {
        viewModelScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val old = tour.value ?: return@launch

            // Update the night in the database to add the end time.
            old.endTimeMilli = System.currentTimeMillis()

            update(old)

            // Set state to navigate to the SleepQualityFragment.
            //_navigateToQuality.value = old
        }
    }

    /**
     * Executes when the CLEAR button is clicked.
     */
    fun onClear() {
        viewModelScope.launch {
            // Clear the database table.
            clear()

            // And clear tonight since it's no longer in the database
            tour.value = null

            // Show a snackbar message, because it's friendly.
            _showSnackbarEvent.value = true
        }
    }


    fun onSetQuality(rate: Double){

        viewModelScope.launch {
            var systemTime: Long = 0L
            var tt = Touring()
            tt.id = systemTime
            tt.foodQuality = rate
            dao.update(tt)
        }

    }

}



/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the SleepDatabaseDao and context to the ViewModel.
 */
class KViewModelFactory(
    private val dataSource: Dao,
    private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KViewModel::class.java)) {
            return KViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



