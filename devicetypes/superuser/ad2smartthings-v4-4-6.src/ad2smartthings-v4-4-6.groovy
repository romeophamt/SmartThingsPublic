/**
 *  AD2SmartThings Home Alarm Device Type
 *  Adds your Honeywell/Ademco home alarm to your SmartThings physical graph
 *  Honeywell/Ademco alarms are usually installed by ADT and other companies.  Check your alarm panel to verify
 *  This SmartThings Device Type Code Works With Arduino Connected To An AD2Pi Wired To A Honeywell/Ademco Home Alarm
 *
 *	By Stan Dotson (stan@dotson.info) and Michael Ritchie
 *
 *	Credits:
 *  Lots of good ideas from @craig whose code can be found at https://gist.github.com/e5b30109fdaec805d474.git
 *  Also relied on architecture enabled by github contributor vassilisv 
 *  Also thanks to Sean Matthews for contributing technical approach to setting AD2Pi address
 *  Date: 2014-08-14
 *  
 *  Zones
 *  This device type supports up to 39 zones.  All the coding is in place
 *  To adjust the number of zones, simply trim the number of zones listed in the <details> argument list.
 *  No other code needs to be modified! (however you can tidy up your event log by deleting  the additional code that refers to unused zones).
 *  
 *  
 *  
 */
 
 // for the UI

preferences {
	
    // The Configuration Command preferences allows input of a configuration command to be sent to AD2Pi.  
    // For example, to change the address to 31 the command would be "ADDRESS=31"
    // After entering the command in setup, you MUST press the "Config" tile to send the configuration command to the AD2Pi
    // Note: this will write to the eeprom of the AD2* so caution should be used to not excessively do this task or it would eventually damage the EEPROM. 
    // This should be preformed only during system setup and configuration!
    // To prevent excessive use, the configCommand value can be reset to null after sending to device
    // pressing the "Config" tile and sending null will harmlessly request the alarm panel to report out its Configurtion into the message tile.
    
    input("configCommand", "text", title: "AD2Pi Configuration Command", description: "for example ADDRESS=31", defaultValue: '', required: false, displayDuringSetup: true)
    input("securityCode", "text", title: "Option: Enter your 4 digit homeowner security code here or in the Arduino sketch",
    	description: "Code entered here takes priority.", defaultValue: "", required: false, displayDuringSetup: true)
    input("panicKey","enum", title: "Select Panic Key Configured By Your Installer", description: "(A:1&*) or (B:*&#) or (C:3&#)", options: ["A","B","C"], required: true, displayDuringSetup: true)
    input("armedInstant", "bool", title: "Use Armed Instant When Arming Stay", description: "Slide To Right For Armed Instant", defaultValue: false, required: false)
}

metadata {
	definition (name: "AD2SmartThings v4.4.6", version: "4.4.6",author: "Stan Dotson and Michael Ritchie") {

        capability "Switch"  // <on> will arm alarm in "stay" mode; same as armStay
        capability "Lock"  // <lock> will arm alarm in "away" mode; same as armAway
        capability "Alarm" // enables <both> or <siren> to  immediately alarm the system
        capability "Motion Sensor" //allows your alarm panel to mimic a motion sensor. The motion sensor will be active when system alarms

        command "disarm"
        command "armStay"
        command "armAway"
        command "chime"
        command "config"
        command "siren"
        command "pressPanicOnce"
        command "pressPanicTwice"

        attribute "system", "string"
        attribute "msg", "string"
        attribute "alertMsg", "string"
	}
}

// Simulator metadata
simulator {
}

// UI tile definitions
tiles {

	standardTile("main", "device.system", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true)
 	{
		state "disarmed", label:"Disarmed", action:"disarm", icon:"st.Home.home2", backgroundColor:"#ffffff"
		state "armedAway", label:"Armed Away", action:"disarm", icon:"st.Home.home3", backgroundColor:"#587498"
		state "armedStay", label:"Armed Stay", action:"disarm", icon:"st.Home.home4", backgroundColor:"#587498"
        	state "alarm", label:"Alarm!", action:"disarm", icon:"st.Home.home3", backgroundColor:"#E86850"
        	state "armingAway", label:"Arming Away", action:"disarm", icon:"st.Home.home3", backgroundColor:"#FFD800"
		state "armingStay", label:"Arming Stay", action:"disarm", icon:"st.Home.home4", backgroundColor:"#FFD800"
	}
 	standardTile("stay", "device.system", width: 1, height: 1, decoration: "flat")
 	{
        	state "disarmed", label: "Stay", action: "armStay", icon:"st.Home.home4", nextState: "armingStay"
        	state "armingStay", label:"Arming Stay", action: "disarm", icon:"st.Home.home4"
        	state "armedStay", label:"Armed Stay", action: "disarm", icon:"st.Home.home4"
  	}
        
  	standardTile("away", "device.system", width: 1, height: 1, decoration: "flat")
	{
       		state "disarmed", label: "Away", action: "armAway", icon:"st.Home.home3", nextState: "armingAway"
        	state "armingAway", label:"Arming Away", action: "disarm", icon:"st.Home.home3" 
       		state "armedAway", label:"Armed Away", action: "disarm", icon:"st.Home.home3"   
  	}
        
 	standardTile("disarm", "device.system", width: 1, height: 1, decoration: "flat")
    	{
       		state "disarmed", label:'Disarm', action: "disarm", icon:"st.Home.home2"   
 	}
    
    	standardTile("panic", "device.panic", width: 1, height: 1, decoration: "flat")
    	{
       		state "disarmed", label: "PANIC", action: "pressPanicOnce", icon:"st.alarm.beep.beep", nextState: "pressedOnce"
        	state "pressedOnce", label: "Press Twice More", action: "pressPanicTwice", icon:"st.alarm.beep.beep", nextState: "pressedTwice"
        	state "pressedTwice", label: "Press Once More", action: "siren", icon:"st.alarm.beep.beep"
        	state "alarm", label:"Panic Alarm", action:"disarm", icon:"st.alarm.beep.beep", backgroundColor:"#E86850" 
  	}
    
    	standardTile("chime", "device.chime", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true, inactiveLabel: false, decoration: "flat")
    	{
       		state "chimeOn", label: 'Chime On',   action: "chime", icon: "st.custom.sonos.unmuted", backgroundColor: "#ffffff", nextState: "sendingChime"
       		state "chimeOff", label: 'Chime Off', action: "chime", icon: "st.custom.sonos.muted", backgroundColor: "#ffffff", nextState: "sendingChime"
       		state "sendingChime", label: 'Sending', action: "chime",icon: "st.custom.sonos.unmuted", backgroundColor: "#ffffff"
  	}

	standardTile("configAD2Pi", "device.config", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true,  decoration:"flat")
	{
    		state "default", label: 'AD2Pi Config', action: "config", icon: "st.secondary.refresh-icon", backgroundColor: "#ffffff"
    	}
        
   	valueTile("msg", "device.msg", width: 3, height:1, inactiveLabel: false, decoration: "flat") 
   	{
		state "default", label:'${currentValue}'
	}
 
/******************************************************************************************************************************************
 BEGINNING OF PERSONALIZED CODE SECTION
 The following tiles to report activity in a given zone.   We dont expect to have to change this section in future upgrades, so...
 To make upgrades easier, just copy and replace the customized zone section below into any updated versions
 Customizations - feel free to customize it for your setup:
   Zone Labels: Change label: 'Zone #' to label: 'custom name'
   Zone Icons: Change icon: "st.security.alarm.clear" to icon: "st.motion.motion.inactive"
     Motion Icon Names: st.motion.motion.inactive, st.motion.motion.active
	 Contact Icon Names: st.contact.contact.closed, st.contact.contact.open
	 Smoke Detector Icon Names: st.alarm.smoke.clear, st.alarm.smoke.smoke

 ******************************************************************************************************************************************/
   
   	standardTile("zone1", "device.zone1", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 1 - Garage & Front Door', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 1 - Garage & Front Door', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
    	standardTile("zone2", "device.zone2", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 2 - Patio Door', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
        	state "active", label: 'Zone 2 - Patio Door', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
   	}
    	standardTile("zone3", "device.zone3", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 3 - Motion', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 3 - Motion', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
 	standardTile("zone4", "device.zone4", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{	
		state "inactive", label: 'Zone 4 - Basement Windows', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 4 - Basement Windows', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
   	standardTile("zone5", "device.zone5", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 5 - Smoke Alarm', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 5 - Smoke Alarm', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
  	standardTile("zone6", "device.zone6", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 6 - Tamper', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 6 - Tamper', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}                      
    	standardTile("zone7", "device.zone7", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 7', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 7', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
	standardTile("zone8", "device.zone8", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 8', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 8', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
	standardTile("zone9", "device.zone9", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 9', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 9', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
	standardTile("zone10", "device.zone10", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 10', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 10', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
   	standardTile("zone11", "device.zone11", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 11', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 11', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
	standardTile("zone12", "device.zone12", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 12', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
        	state "active", label: 'Zone 12', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
   	}
	standardTile("zone13", "device.zone13", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 13', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 13', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
 	standardTile("zone14", "device.zone14", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 14', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 14', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
   	standardTile("zone15", "device.zone15", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 15', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 15', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
  	standardTile("zone16", "device.zone16", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 16', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 16', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}                      
  	standardTile("zone17", "device.zone17", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 17', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 17', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
  	standardTile("zone18", "device.zone18", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 18', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 18', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
  	standardTile("zone19", "device.zone19", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 19', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 19', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
    	standardTile("zone20", "device.zone20", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 20', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 20', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
   	standardTile("zone21", "device.zone21", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 21', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 21', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
	standardTile("zone22", "device.zone22", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 22', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
        	state "active", label: 'Zone 22', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
   	}
	standardTile("zone23", "device.23", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 23', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 23', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
 	standardTile("zone24", "device.zone24", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 24', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 24', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
   	standardTile("zone25", "device.zone25", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 25', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 25', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
  	standardTile("zone26", "device.zone26", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 26', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 26', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}                      
  	standardTile("zone27", "device.zone27", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 27', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 27', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
  	standardTile("zone28", "device.zone28", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 28', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 28', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
  	standardTile("zone29", "device.zone29", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 29', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 29', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
    	standardTile("zone30", "device.zone30", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 30', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 30', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}   
   	standardTile("zone31", "device.zone31", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 31', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 31', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
	standardTile("zone32", "device.zone32", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
	{
		state "inactive", label: 'Zone 32', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
        	state "active", label: 'Zone 32', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
   	}
	standardTile("zone33", "device.33", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 33', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 33', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
 	standardTile("zone34", "device.zone34", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 34', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 34', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
   	standardTile("zone35", "device.zone35", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 35', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 35', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}
  	standardTile("zone36", "device.zone36", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 36', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 36', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}                      
  	standardTile("zone37", "device.zone37", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 37', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 37', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
  	standardTile("zone38", "device.zone38", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 38', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 38', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}  
  	standardTile("zone39", "device.zone39", width: 1, height:1, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) 
    	{
		state "inactive", label: 'Zone 39', icon: "st.security.alarm.clear", backgroundColor: "#ffffff"
		state "active", label: 'Zone 39', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
	}


/******************************************************************************************************************************************
  To customize your device type to the number of zones in your system, simply trim the argument for the <details> command
  to equal the number of zones. 
  For example, for 1 zone systems:
    	details(["main", "disarm","chime","stay", "away", "panic", "msg", "zone1", "configAD2Pi"])
  For example, for 4 zones:
    	details(["main", "disarm","chime","stay", "away", "panic", "msg", "zone1","zone2", "zone3","zone4", "configAD2Pi"])
  The maximum number of zones supported by this device type is 36 zones:
   		details(["main", "disarm","chime","stay", "away", "panic", "msg", "zone1","zone2", "zone3","zone4","zone5","zone6","zone10","zone11","zone12","zone13","zone14","zone15","zone16","zone17","zone18","zone19","zone20","zone21","zone22","zone23","zone24","zone25","zone26","zone27","zone28","zone29","zone30","zone31","zone32","zone33","zone34","zone35","zone36","zone37","zone38","zone39", "configAD2Pi"])
  
  
  Note: Zones 7,8,9, 95,96 are typically reserved for Panic, Duress, Tamper, Panic and Panic, respectively.  No tile is provided for them
  Zones do not have to display in order of zone number.  Feel free to reorder zones to your liking.  For example:
  	details(["main", "disarm","chime","stay", "away", "panic", "msg", "zone4","zone2", "zone3","zone1", "configAD2Pi"])
******************************************************************************************************************************************/

	main(["main"])
	details(["main", "disarm","chime","stay", "away", "panic", "msg", "zone1","zone2", "zone3","zone4","zone5","zone6","configAD2Pi"])
}

/******************************************************************************************************************************************
END OF PERSONALIZED CODE SECTION
******************************************************************************************************************************************/
 

// Parse incoming device messages to generate events
def parse(String description) {
    def isDebugEnabled = false
  	
  	def result = []
    def rawMsg = zigbee.parse(description)?.text
    if (isDebugEnabled) log.debug "rawMsg: ${rawMsg}"
    
    if (rawMsg.contains("ping") || rawMsg.equals("") || rawMsg == null) {
    	// Do Nothing
    
    } else if (rawMsg.contains("...."))  {
		result << createEvent(name: "msg", value: "Having Trouble Sending")   
    } else if (rawMsg.contains("|")) {        
        //0:powerStatus, 1:chimeStatus, 2:alarmStatus, 3:activeZone, 4:inactiveList, 5:keypadMsg
        String[] msgSplit = rawMsg.toString().split("[|]", 7);

        def powerStatus = msgSplit[0].trim()
        def chimeStatus = msgSplit[1].trim()
        def alarmStatus = msgSplit[2].trim()
		def activeZone = msgSplit[3].trim()
        def inactiveList = msgSplit[4].trim()
        def keypadMsg = msgSplit[5].trim()

        if (powerStatus != "" && powerStatus != null) {
            if (powerStatus == "AC") {
                result << createEvent(name: "alertMsg", value: "Alarm on AC Power")
            } else if (powerStatus == "BN") {
                result << createEvent(name: "alertMsg", value: "Alarm on Battery Power: Fully Charged")
            } else if (powerStatus == "BL") {
                result << createEvent(name: "alertMsg", value: "Alarm on Battery Power: Low Charge")
            }
            if (isDebugEnabled) log.debug "powerStatus: ${powerStatus}"
        }
		
        if (chimeStatus != "" && chimeStatus != null) {
            result << createEvent(name: "chime", value: "${chimeStatus}", displayed: true, isStateChange: true, isPhysical: true)
            if (isDebugEnabled) log.debug "chimeStatus: ${chimeStatus}"
        }
			
        if (alarmStatus != "" && alarmStatus != null) {
            if (alarmStatus == "alarm") {
                result << createEvent(name: "alertMsg", value: "Alarm is sounding!!")
                if (isDebugEnabled) log.debug "alertMsg: Alarm is sounding!!"
            }
            result << createEvent(name: "system", value: "${alarmStatus}", displayed: true, isStateChange: true, isPhysical: true)
            if (isDebugEnabled) log.debug "alarmStatus: ${alarmStatus}"
        }
        
        if (activeZone.equals("") || activeZone == null) {
            // Do Nothing
        } else {
            result << createEvent(name: "zone${activeZone.trim()}", value: "active", descriptionText: keypadMsg, displayed: true, isStateChange: true, isPhysical: true)
            if (isDebugEnabled) log.debug "Created active event for zone${activeZone.trim()}"
        }
        
        //process inActiveList
        if (inactiveList.equals("") || inactiveList == null) {
            // Do Nothing
        } else {
            if (inactiveList.toString().indexOf("allClear") > -1) {
                def numZones  = inactiveList.substring(inactiveList.indexOf(":") + 1).toInteger()
                inactiveList = ""
                for (int i = 1; i <= numZones; i++) {
					inactiveList = inactiveList + i + ","
            	}
            }
            def inactiveArray = inactiveList.toString().split(",");     
            for (int i = 0; i < inactiveArray.size(); i++) {
                if (device.currentValue("zone${inactiveArray[i].trim()}") == "active") {
                	result << createEvent(name: "zone${inactiveArray[i].trim()}", value: "inactive", displayed: true, isStateChange: true, isPhysical: true)
                	if (isDebugEnabled) log.debug "Created inactive event for zone${inactiveArray[i].trim()}"
                }
            }
        }
        
        if (keypadMsg != "" && keypadMsg != null) {
	    	result << createEvent(name: "msg", value: keypadMsg, displayed: false)  //displays keypad message to message tile; change to true if you want to also log in "Recently"
            if (keypadMsg == "Alarm not ready cannot arm") {
                if (isDebugEnabled) log.debug "alertMsg: ${keypadMsg}"
            	result << createEvent(name: "alertMsg", value: keypadMsg)
            }
            if (isDebugEnabled) log.debug "keypadMsg: ${keypadMsg}"
        }
    } else {
		result << createEvent(name: "msg", value: rawMsg, displayed: false)
    }
    return result
}

// Commands sent to the device
def on() { //use to turn on alarm while home
	armStay()
}

def off() {
	disarm()
}

def lock() { //use to turn on alarm in away mode
	armAway()
}

def unlock() {
	disarm()
}

def armAway() {
    //0:msgType, 1:msgSecurityCode, 2:msgCmd
    def securityCodeVal = (securityCode) ? securityCode : ""
    zigbee.smartShield(text: "CODE|"+ securityCodeVal + "|2").format()   
}

def disarm() {
	sendEvent(name: "system", value: "disarmed", displayed: true, isStateChange: true, isPhysical: true)
    sendEvent(name: "panic", value: "disarmed", displayed: false, isStateChange: true, isPhysical: true)
    //0:msgType, 1:msgSecurityCode, 2:msgCmd
    def securityCodeVal = (securityCode) ? securityCode : ""
    zigbee.smartShield(text: "CODE|"+ securityCodeVal + "|1***").format()
}

def armStay() {
    //0:msgType, 1:msgSecurityCode, 2:msgCmd
    def securityCodeVal = (securityCode) ? securityCode : ""
    def alarmCode = "|3"
    if (armedInstant) {
    	alarmCode = "|7"
    }
    zigbee.smartShield(text: "CODE|" + securityCodeVal + alarmCode).format()
}

def siren() {
    //log.debug "sent panic"
    //0:msgType, 1:msgSecurityCode, 2:msgCmd
    zigbee.smartShield(text: "FUNC||" + panicKey).format()
}

def chime() {
    //0:msgType, 1:msgSecurityCode, 2:msgCmd
    def securityCodeVal = (securityCode) ? securityCode : ""
    zigbee.smartShield(text: "CODE|" + securityCodeVal + "|9").format()
}

def config() {
	// pressing the "Config" tile on the AD2Pi will normally request the alarm panel to report out its Configurtion into the message tile.
    // If a Configuration Command was provided as input into the Preferences, this method will send the command down to the Arduino
    // which will write to the eeprom of the AD2Pi 

    log.debug "sending AD2Pi Config Command: ${configCommand}"
    //0:msgType, 1:msgSecurityCode, 2:msgCmd
    zigbee.smartShield(text: "CONF||${configCommand}").format()
}

def pressPanicOnce() {
    log.debug "pressed panic key once"
    sendEvent(name: "panic", value: "pressedOnce", displayed: true, isStateChange: true, isPhysical: true)
}

def pressPanicTwice() {
    log.debug "pressed panic key twice"
    sendEvent(name: "panic", value: "pressedTwice", displayed: true, isStateChange: true, isPhysical: true)
}