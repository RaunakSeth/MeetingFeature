package com.example.meetingfeature

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.meetingfeature.ui.theme.MeetingFeatureTheme
import com.google.gson.internal.bind.util.ISO8601Utils
import com.maxkeppeler.sheets.date_time.DateTimeDialog
import com.maxkeppeler.sheets.date_time.models.DateTimeConfig
import com.maxkeppeler.sheets.date_time.models.DateTimeSelection
import io.appwrite.models.DocumentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState as rememberUseCaseState

class MainActivity : ComponentActivity() {
    private lateinit var AppwriteViewModel: MeetViewModelClass
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppwriteViewModel=ViewModelProvider(this)[MeetViewModelClass::class.java]
            AppwriteViewModel.init(applicationContext)
            MeetingFeatureTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screen(AppwriteViewModel)
                }
            }
        }
    }
}

fun <T : Any> mapToObject(map: Map<String, Any>, clazz: KClass<T>) : T {
    //Get default constructor
    val constructor = clazz.constructors.first()

    //Map constructor parameters to map values
    val args = constructor
        .parameters
        .map { it to map.get(it.name) }
        .toMap()

    //return object from constructor call
    return constructor.callBy(args)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(AppwriteViewModel:MeetViewModelClass,modifier: Modifier = Modifier) {
    meetortask(AppwriteViewModel,"Meeting")
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun meetortask(AppwriteViewModel:MeetViewModelClass,type:String ,modifier: Modifier = Modifier)
{
    lateinit var response: DocumentList<Map<String, Any>>
    var i by remember{ mutableStateOf(0) }
    var meetDetails:MeetDetails by remember {
        mutableStateOf(MeetDetails("","","","","","",""))
    }
    val meets:MutableList<MeetDetails> by remember {
        mutableStateOf(mutableListOf<MeetDetails>())
    }
    var frag by remember { mutableStateOf("Create") }
    val options= mutableStateListOf<String>("Create $type","View $type")
    var selectedIndex by remember{
        mutableStateOf(0)
    }
    CoroutineScope(Dispatchers.IO).launch {
        response = AppwriteViewModel.getfromDatabase()

        for (j in response.documents) {
            val element =mapToObject(j.data, AppwriteDataDetailsClass::class)
            val dateTime = LocalDateTime.parse(element.MeetDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val minutes=if(dateTime.minute.toInt()<10){"0${dateTime.minute}"}else{"${dateTime.minute}"}
            val converted=MeetDetails(id=j.id,MeetDomain=element.MeetDomain,MeetTime= LocalTime.parse("${dateTime.hour}:${minutes}").format(DateTimeFormatter.ofPattern("h:mma")),MeetDate="${dateTime.dayOfMonth}-${dateTime.month}-${dateTime.year}",MeetPlace=element.MeetPlace, MeetDescription = element.MeetDescription,MeetYear=element.MeetYear)
            if(meets.contains(converted)==false)
            {
            meets.add(i,converted)
            i++
            }
        }
    }
    Column(modifier=modifier.padding(20.dp)){
        Text(
            text = type,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(5.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center,
        )
        SingleChoiceSegmentedButtonRow(modifier = modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(20.dp)){
            options.forEachIndexed(){ index,option->
                SegmentedButton(
                    selected = selectedIndex== index,
                    onClick = { selectedIndex=index
                              if(index==0)
                              frag="Create"
                              else
                              frag="View"
                              },
                    shape = SegmentedButtonDefaults.itemShape(index = index,
                        count = options.size)) {
                    Text(text = option,
                        modifier = modifier
                            .padding(5.dp))
                }
            }
        }
        if(frag == "Create")
        {
            form(AppwriteViewModel=AppwriteViewModel, meets = meets)
        }
        else
        {
            view(modifier, meets)
        }
    }
}
@Composable
fun view(modifier: Modifier=Modifier,
         meets: MutableList<MeetDetails>)
{
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = meets) { meet ->
            CardContent(meet.MeetDomain, meet.MeetTime, meet.MeetDate, meet.MeetPlace, meet.MeetYear)
        }
    }
}

@Composable
fun CardContent(
    Domain: String = "Android",
    MeetTime: String = "13:00",
    MeetDate: String = "01/01/2024",
    MeetPlace: String = "H-608",
    MeetYear: String = "1st Year"
) {
    Column {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .defaultMinSize(260.dp)
                .height(270.dp)
                .padding(10.dp),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(2f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.wrapContentSize(),
                        color = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text(
                            text = MeetYear,
                            fontSize =  12.sp,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$Domain Domain",
                        fontSize =  24.sp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(text = "Meet Time : $MeetTime")
                    Text(text = "Meet Date : $MeetDate")
                    Text(text = "Meet Place : $MeetPlace")

                    Spacer(modifier = Modifier.height(2.dp))
                    OutlinedButton(
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.White
                        ),
                        onClick = { }
                    ) {
                        Text(
                            text = "Mark Attendance",
                            fontSize =  13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(width = 100.dp, height = 140.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentScale = ContentScale.Crop,
                        contentDescription = null
                    )
                }
            }
        }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun form(AppwriteViewModel:MeetViewModelClass,modifier: Modifier = Modifier,meets: MutableList<MeetDetails>)
{
    var sucess:Boolean=false
    val context= LocalContext.current
    var enabled by remember { mutableStateOf(true)}
    val ButtonState= remember { mutableStateOf(true) }
    var list1=listOf<String>("Web","Android","AR/VR","Machin Learning","CP/DSA","Graphics","Content")
    var list2=listOf<String>("1st Year","2nd Year","3rd Year","4th Year")
    val DateTime = rememberUseCaseState()
    val selectedDateTime = remember { mutableStateOf<LocalDateTime?>(null) }
    var meetDetails:MeetDetails by remember { mutableStateOf(MeetDetails("","","","","","","")) }
    val scrollState = rememberScrollState()
    DateTimeDialog(
        state = DateTime,
        selection = DateTimeSelection.DateTime { newDateTime ->
            selectedDateTime.value = newDateTime
            Log.d("DATE AND TIME","${selectedDateTime.value}")
            ButtonState.value=false
        },
        config = DateTimeConfig(

        )
    )
    Column(modifier = Modifier
        .verticalScroll(state = scrollState)) {
        Text(text = "Meet Description:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),)
        Spacer(modifier = modifier.height(10.dp))
        var description=edittext(modifier= modifier
            .fillMaxWidth()
            .height(100.dp))
        Spacer(modifier = modifier.height(10.dp))
        Text(text = "Meet Domain:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold
            ),)
        Spacer(modifier = modifier.height(10.dp))
        var domain=dropdownmenu(
            list1,
            modifier= modifier
                .fillMaxWidth()
                .height(50.dp),
            "Web")
        Spacer(modifier = modifier.height(10.dp))
        Text(text = "Meet Date & Time:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold
            ),)
        Spacer(modifier = modifier.height(10.dp))
        if(ButtonState.value)
        {
        Button(onClick = {
            DateTime.show()
        }) {
            Text(text="Set Time")
        }
        }
        else
        {
            Text(
                text = "Meet date and time set as ${selectedDateTime.value?.dayOfMonth}-${selectedDateTime.value?.month}-${selectedDateTime.value?.year} at ${selectedDateTime.value?.hour}:${selectedDateTime.value?.minute}",
                modifier=Modifier.clickable {
                    DateTime.show()
                },
                color = MaterialTheme.colorScheme.primary,

                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold

                ),)
        }
        Spacer(modifier = modifier.height(10.dp))
        Text(text = "Meet Year:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),)
        Spacer(modifier = modifier.height(10.dp))
        var year=dropdownmenu(
            list2,
            modifier= modifier
                .fillMaxWidth()
                .height(50.dp),
            "1st Year")
        Spacer(modifier = modifier.height(10.dp))
        Text(text = "Meet Place:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),)
        Spacer(modifier = modifier.height(10.dp))
        var place= edittext(
            modifier= modifier
                .fillMaxWidth()
                .height(50.dp))
        Spacer(modifier = modifier.height(10.dp))
        Button(
            onClick = {

                if (selectedDateTime.value != null)
                {
                    Toast.makeText(
                        context,
                        "Your Meet is scheduled at ${selectedDateTime.value?.dayOfMonth}-${selectedDateTime.value?.month}-${selectedDateTime.value?.year} at ${selectedDateTime.value?.hour}:${selectedDateTime.value?.minute}",
                        Toast.LENGTH_LONG
                    ).show()
                    meetDetails=MeetDetails("",domain,"${selectedDateTime.value?.hour}:${selectedDateTime.value?.minute}","${selectedDateTime.value?.dayOfMonth}-${selectedDateTime.value?.month}-${selectedDateTime.value?.year}",place.value,description.value,year,selectedDateTime.value.toString())
                    Log.d("Submit","Meet Description: ${description.value}\n" +
                            "Meet Domain: $domain \n " +
                            "Meet Time :${selectedDateTime.value?.dayOfMonth}-${selectedDateTime.value?.month}-${selectedDateTime.value?.year} at ${selectedDateTime.value?.hour}:${selectedDateTime.value?.minute}\n" +
                            "Meet Year: $year")
                   CoroutineScope(Dispatchers.IO).launch {
                      sucess=AppwriteViewModel.storetoDatabase(meetDetails,meets,context)
                   }
                    if(sucess)
                    {
                        Toast.makeText(context,"Data was successfully inserted in data base",Toast.LENGTH_LONG)
                    }

                }
                else {
                    Toast.makeText(
                        context,
                        "Set Meet Time First",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier= modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)) {
            Text(text = "Submit")
        }
}
}
@Composable
fun edittext(modifier: Modifier = Modifier): MutableState<String>
{
    val textState= remember { mutableStateOf("") }
    TextField(value = textState.value,
        modifier= modifier,
        onValueChange = {
            textState.value=it
        }
    )
    return textState
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dropdownmenu(list:List<String>,modifier: Modifier = Modifier,default:String): String {
    var isExpanded by remember{ mutableStateOf(false) }
    var domain by remember{ mutableStateOf(default) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded=it },
        modifier=modifier
    ) {
        TextField(
            value = domain,
            placeholder = { Text(text = "${list[0]}")},
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            colors=ExposedDropdownMenuDefaults.textFieldColors(),
            modifier=modifier.menuAnchor())
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded=false})
        {
            for(i in list)
            {
            DropdownMenuItem(
            text = {
                Text(text = "$i")
                   },
            onClick = { domain="$i"
            isExpanded=false
            })
            }
        }
    }
    return domain
}

@Preview(showBackground = true)
@Composable
fun ViewPreview() {
    MeetingFeatureTheme {
       CardContent()
    }
}
//@Preview(showBackground = true,
//    widthDp = 360,
//    heightDp = 720)
//@Composable
//fun MeetingPreview() {
//    MeetingFeatureTheme {
//        Screen()
//    }
//}