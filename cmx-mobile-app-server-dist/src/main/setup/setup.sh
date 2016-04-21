#!/bin/sh
# Where am I?
setupHome=`dirname $0`
setupFile="$0"

############################################################
#
# Global variables
#
############################################################

# Global return codes
RC_OK=0
RC_ERROR=1
RC_BACK=2
RC_SKIP=3
RC_EXEC=0

# The input value that means "go back"
READ_BACK="^"

# Input entry codes
ENTER_INPUT="Yes"
SKIP_INPUT="Skip"
RESET_INPUT="UseDefault"
DISABLE_INPUT="Disable"

# Well known values
YES="yes"
NO="no"
NO_IP="none"
NO_ROUTE="none"
CURRENT_TIME="skip"
SKIP="skip"
DEF_VALUE="default"

AUDIT_VERIFY="verify"
AUDIT_LOG_FILE=$setupHome/setup.mylog
#Remove any existing audit log file
rm -rf $AUDIT_LOG_FILE

# Constants for requesting next and previous states
# from goto_state
GOTO_NEXT="next"
GOTO_PREV="prev"

# Well-known states
STATE_VIEW_CONFIG="view_config"
STATE_INITIAL="group_cred"
STATE_FINAL="done"
STATE_NONE="none"
# State sequence
GOTO_list="
    view_config
    group_cred
    cred_username
    cred_password
    group_sdk
    sdk_username
    sdk_password
    group_server_port
    server_port
    group_sdk_port
    sdk_port
    group_log_level
    log_level
    verify
    $STATE_FINAL"

############################################################
#
# Utility functions
#
############################################################

# Read an input variable, defaulting the result to its
# current value.  Check for the back value, and return
# whether or not to go back.
read_default() {
    prompt="$1"
    default="$2"

    # Build the prompt
    if [ -n "$default" ]; then
        real_prompt="$prompt [$default]: "
    else
        real_prompt="$prompt: "
    fi

    # Output prompt and read value
    read -p "$real_prompt" read_result

    # Check if menu mode is enabled
    if [ $enableMenu == $NO ]; then
           # Check read_result
       if [ "$read_result" = "^" ]; then
            # Go back
            return $RC_BACK
           fi
    fi

    # Check if user entered any value
    if [ -n "$read_result" ]; then
        return $RC_OK
    else
        # Use default (leave variable as-is)
        read_result="$default"
        return $RC_OK
    fi
}

read_number() {
    prompt="$1"
    default="$2"
    rangeLo="$3"
    rangeHi="$4"

    nonNumbers="ABCD"
    while [ -n "$nonNumbers" ]; do
        read_default "$prompt" "$default"
        [ "$?" = "$RC_BACK" ] && [ "$enableMenu" == "$NO" ] && return $RC_BACK
        nonNumbers=`echo "$read_result" | egrep -v "[0-9]+"`
        if [ -n "$nonNumbers" ]; then
            echo "Please enter only numbers."
            nonNumbers="ABCD"
        elif [ $read_result -lt $rangeLo ]; then
            echo "Value must be > $rangeLo"
            nonNumbers="ABCD"
        elif [ $read_result -gt $rangeHi ]; then
            echo "Value must be < $rangeHi"
            nonNumbers="ABCD"
        fi
    done

    return $RC_OK
}

# Read a password and confirm it.  Check for the back value,
# and return whether or not to go back.
read_password() {
    prompt="$1"
    confirm="$2"

    # Build the prompts
    real_prompt="$prompt: "
    real_confirm="$confirm: "

    # Repeat until there is a match.
    match=0
    while [ $match -eq 0 ]; do
        # Output prompt and read value
        read -s -p "$real_prompt" read_result
        echo "" >&2

    # Check if menu is disabled
    if [ $enableMenu == $NO ]; then
            # Check read_result
            if [ "$read_result" = "^" ]; then
                # Go back
                    return $RC_BACK
        fi
    fi

    # Check read result
        if [ -n "$read_result" ]; then
            # Confirm
            read -s -p "$real_confirm" confirm_result
            echo "" >&2

        # Check if menu is disabled
        if [ $enableMenu == $NO ]; then
                # Check read result
                if [ "$read_result" = "^" ]; then
                    # Go back
                    return $RC_BACK
        fi
        fi

        # Check read result
            if [ "$read_result" != "$confirm_result" ]; then
                echo "The two values do not match.  Please try again." >&2
            else
                match=1
            fi
        else
            # Nothing entered
            echo "You must enter a value." >&2
        fi
    done
    return $RC_OK
}

# Return the previous or next state from the current state,
# as requested.
goto_state() {
    dir="$1"
    cur="$2"

    # Generate an awk script to find the next or previous state
    # from the state list.
    awkscript="BEGIN { prev = \"\" }
        /^ *$cur *$/ { if (dir == \"$GOTO_PREV\") {
                print (prev == \"\") ? \"$cur\" : prev
                exit 0
            } else if (dir == \"$GOTO_NEXT\") {
                getline nextline
                print nextline
                exit 0
            } else {
                exit 1
            } }
        { gsub(\" \", \"\"); prev = \$0 }"

    # Check parameters.
    result=`echo "$GOTO_list" | awk -v dir=$dir "$awkscript"`
    if [ $? -ne 0 ]; then
        # Who knowns what requested
        echo "Internal error: goto_state $*" >&2
        return $RC_ERROR
    else
        echo "$result" | sed -e 's/ //g'
        return $RC_OK
    fi
}

# Return the previous or next state that should not be skipped
# from the current state, as requested.
goto_valid_state() {
    dir="$1"
    cur="$2"

    # Loop until we get to a state that should not be skipped.
    s=`goto_state $dir $cur`; rc=$?
    [ $rc -ne $RC_OK ] && return $rc
    if [ $s = $STATE_FINAL ]; then
        state=$s
        return $RC_OK
    fi

    # Check if we are attempting to transition to view config state
    if [ $s == $STATE_VIEW_CONFIG ]; then
    # If yes, silently switch over to initial state
    s=$STATE_INITIAL
    fi

    test_$s $dir
    while [ $? -eq $RC_SKIP ]; do
        s=`goto_state $dir $s`; rc=$?
        [ $rc -ne $RC_OK ] && return $rc
        if [ $s = $STATE_FINAL ]; then
            state=$s
            return $RC_OK
        fi
        test_$s $dir
    done

    # Echo it, and return
    state=$s
    return $RC_OK
}

# Return 0 if the specified argument is a valid IP address
is_ip_format() {
    ip="$1"
    num='(([0-9])|([1-9][0-9])|([1-9][0-9][0-9])|(2[0-4][0-9])|(25[0-5]))'
    addr="^$num\\.$num\\.$num\\.$num\$"
    echo "$ip" | egrep -q "$addr"
    return $?
}

# Return 0 if specified address is valid IP.  Also outputs error msgs.
check_ip() {
    ip="$1"
    name="$2"
    is_ip_format "$ip"
    if [ $? -ne 0 ]; then
        echo "$name must be in the form #.#.#.#, where # is 0 to 255 or hexadecimal : separated v6 address" >&2
        return $RC_ERROR
    elif [ "$read_result" = "0.0.0.0" ]; then
        echo "$name cannot be 0.0.0.0" >&2
        return $RC_ERROR
    elif [ "$read_result" = "255.255.255.255" ]; then
        echo "$name cannot be 255.255.255.255" >&2
        return $RC_ERROR
    fi
    return $RC_OK
}

check_ip_subnet() {
    ip_subnet="$1"
    index=`expr index "$ip_subnet" /`
    ipaddr=`expr substr $ip_subnet 0 $index`
    echo $ipaddr
    subnet=`expr substr $ip_subnet 14 15`
    echo $subnet
}

# Find an unsued filename based on the specified filename.
find_unused_filename() {
    fn="$1"
    index=1
    newfn="${fn}.$index"
    while [ -f "$newfn" -o -d "$newfn" ]; do
        index=`expr $index + 1`
        newfn="${fn}.$index"
    done
    echo "$newfn"
}

# Move the specified file out of the way.
move_and_save_file() {
    fn="$1"

    # This used to use find_unused_filename, but that was
    # causing excessive clutter.  So, just save the current
    # configuration.
    savefn="$fn.cfgsav"
    rm -f $savefn 2> /dev/null
    mv "$fn" "$savefn" 2> /dev/null
    rc=$?
    echo "$savefn"
    return $rc
}

validate_group_reply() {
    rr="`echo \"$read_result\" | sed -e 's/ //g' | tr A-Z a-z`"

    input_group_reply="$SKIP_INPUT"

    if [ "$rr" = "y" -o "$rr" = "yes" ]; then
        input_group_reply="$ENTER_INPUT"
        return $RC_OK
    elif [ "$rr" = "s" -o "$rr" = "skip" ]; then
        input_group_reply="$SKIP_INPUT"
        return $RC_OK
    elif [ "$rr" = "u" -o "$rr" = "default" -o "$rr" = "usedefault" ]; then
        input_group_reply="$RESET_INPUT"
        return $RC_OK
    fi

    echo 'Please enter "yes", "skip" or "use default".' >&2
    return $RC_ERROR
}

validate_group_reply_consider_disable() {
    rr="`echo \"$read_result\" | sed -e 's/ //g' | tr A-Z a-z`"

    input_group_reply="$SKIP_INPUT"

    if [ "$rr" = "y" -o "$rr" = "yes" ]; then
        input_group_reply="$ENTER_INPUT"
        return $RC_OK
    elif [ "$rr" = "s" -o "$rr" = "skip" ]; then
        input_group_reply="$SKIP_INPUT"
        return $RC_OK
    elif [ "$rr" = "u" -o "$rr" = "default" -o "$rr" = "usedefault" ]; then
        input_group_reply="$RESET_INPUT"
        return $RC_OK
    elif [ "$rr" = "d" -o "$rr" = "disable" ]; then
        input_group_reply="$DISABLE_INPUT"
        return $RC_OK
    fi

    echo 'Please enter "yes", "skip", "disable" or "use default".' >&2
    return $RC_ERROR
}

validate_passwd()
{
   valPass="$1"
   echo $valPass | egrep [^[:print:]] > /dev/null 2>&1
   if [ $? = 0 ]; then
      echo; echo 'Invalid characters. Try again.'; echo
      return $RC_ERROR
   fi

   echo "$valPass" | grep -q ' '
   if [ $? -eq 0 ]; then
       echo "The password cannot have spaces." >&2
       return $RC_ERROR
   fi

   check_passwd_strength="no"
   if [ -n "$strong_passwd_enable" ]; then
       if [ "$strong_passwd_enable" = "yes" ]; then
            check_passwd_strength="yes"
        fi
    else
        if [ "$curr_strong_passwd_enable" = "yes" ]; then
            check_passwd_strength="yes"
        fi
    fi

   
    LENGTH=`echo -n $valPass | wc -c`
    if [ "$check_passwd_strength" = "no" ]; then
        if [ $LENGTH -lt 8 ]; then
            echo; echo 'Password must be 8 characters long. Try again.'; echo
            return $RC_ERROR
        fi
        return $RC_OK
    fi


    if [ -n "$min_len" ]; then
        minPassLen="$min_len"
    elif [ -n "$curr_min_len" ]; then
        minPassLen="$curr_min_len"
    else
        minPassLen=8
    fi

    if [ $LENGTH -lt $minPassLen ]; then
        echo; echo "Password must be $minPassLen characters long. Try again."; echo
        return $RC_ERROR
    fi
        
   #Consecutive character check
   INDEX=1
   LENGTH=`echo -n $valPass | wc -c`
   RANGE=`expr $LENGTH - 2`
   BAD="false"

   while [ $INDEX -le $RANGE ]; do

      INDEXPLUSONE=`expr $INDEX + 1`
      INDEXPLUSTWO=`expr $INDEX + 2`

      FIRST=`echo $valPass | cut -b$INDEX`
      SECOND=`echo $valPass | cut -b$INDEXPLUSONE`

      if [ "$FIRST" != "$SECOND" ]; then
         INDEX=`expr $INDEX + 1`
         continue
      fi

      THIRD=`echo $valPass | cut -b$INDEXPLUSTWO`

      if [ "$SECOND" != "$THIRD" ]; then
         INDEX=`expr $INDEX + 1`
         continue
      fi

      echo;echo "Three consecutive characters can not be the same. Try again.";echo
      BAD="true"
      break
   done
   if [ $BAD = true ]; then
      return $RC_ERROR 
   fi

   #Character class check
   CAPITOL=0
   LOWERCASE=0
   DIGIT=0
   PUNCT=0
   INDEX=1
   while [ $INDEX -le $LENGTH ]; do

      CHAR=`echo $valPass | cut -b$INDEX`

      echo $CHAR | egrep [[:upper:]] > /dev/null 2>&1
      if [ $? = 0 ]; then
        CAPITOL=`expr $CAPITOL + 1`
      fi

      echo $CHAR | egrep [[:lower:]] > /dev/null 2>&1
      if [ $? = 0 ]; then
         LOWERCASE=`expr $LOWERCASE + 1`
      fi

      echo $CHAR | egrep [[:digit:]] > /dev/null 2>&1
      if [ $? = 0 ]; then
         DIGIT=`expr $DIGIT + 1`
      fi

      echo $CHAR | egrep [[:punct:]] > /dev/null 2>&1
      if [ $? = 0 ]; then
         PUNCT=`expr $PUNCT + 1`
      fi
      INDEX=`expr $INDEX + 1`
   done

    if [ $CAPITOL -lt 2 -o $LOWERCASE -lt 2 -o $DIGIT -lt 2 -o $PUNCT -lt 2 ]; then
        echo "UP = $CAPITOL, LO = $LOWERCASE, DIGIT = $DIGIT, PUNCT = $PUNCT"
        echo;echo "Password must contain 2 uppercase, 2 lowercase letters,"
        echo "2 digits and 2 special characters. Try again."; echo

        return $RC_ERROR
    fi

    return $RC_OK
}

#############################################################
# Function to check for user's Wizard/Manual setup preference
#############################################################
checkConfigMode() {

cat <<EOF >&2

--------------------------------------------------------------

Welcome to the Cisco CMX Mobile App Server setup.

You may exit the setup at any time by typing <Ctrl+C>.  

--------------------------------------------------------------

EOF

   while true
    do
        read_default "Would you like to configure CMX Mobile App Server using menu options (yes/no)"

        rr="`echo \"$read_result\" | sed -e 's/ //g' | tr A-Z a-z`"
        if [ "$rr" = "y" -o "$rr" = "yes" ]; then
        enableMenu="$YES"   
        break;
        elif [ "$rr" = "n" -o "$rr" = "no" ]; then
        enableMenu="$NO"
        break;
        fi
    done
   return $RC_OK
}

checkSetupMode() {

cat <<EOF >&2

Enter whether you would like to set up the initial 
parameters manually or via the setup wizard. 

EOF

    read_default "Setup parameters via Setup Wizard (yes/no)" "$YES"

    rr="`echo \"$read_result\" | sed -e 's/ //g' | tr A-Z a-z`"
    if [ "$rr" = "y" -o "$rr" = "yes" ]; then
        return 0
    fi
    return 1
}

checkNextLoginSetup() {

    read_default "Would you like the Setup Wizard to be re-run at next login (yes/no)" "$NO"

    rr="`echo \"$read_result\" | sed -e 's/ //g' | tr A-Z a-z`"
    if [ "$rr" = "y" -o "$rr" = "yes" ]; then
        return 0
    fi
    return 1
}

printUsage() {
    cat << EOF >&2
Usage: $0 [-f defaults_filename] [-l] [-o setup_log_file]

EOF
}

removeShellScriptFromBoot() {
    # Remove the setup shell.
    usermod -s "/bin/bash" root
    [ $? -ne 0 ] && return $RC_ERROR
}

############################################################
#
# Action routines
#
# test_<state> - Return whether or not to skip the step.
# prompt_<state> - Output a message explaining what is
#         being requested.
# read_<state> - Output a prompt, and read the value into
#         a shell variable.
# validate_<state> - Validate whether or not the read
#         value is acceptable.  If so, set the variable.
# apply_<state> - Apply the configuration.
#
############################################################

loadSetup() {
    for i in $GOTO_list; do
        [ $i = $STATE_FINAL ] && return $RC_OK
        . $setupHome/setup_$i
        [ $? -ne 0 ] && return $RC_ERROR
    done

    # Should not get here
    return $RC_ERROR
}

performSetup() {
    for i in $GOTO_list; do
        [ $i = "resetmse" ] && return $RC_OK
        [ $i = $STATE_FINAL ] && return $RC_OK
        apply_$i
        [ $? -ne 0 ] && return $RC_ERROR
    done

    # Should not get here
    return $RC_ERROR
}

defaultSetupMode() {
# Output greeting
cat <<EOF >&2

--------------------------------------------------------------

CMX Mobile App Server Setup.

Please enter the requested information.  At any prompt,
enter ^ to go back to the previous prompt. You may exit at 
any time by typing <Ctrl+C>.  

You will be prompted to choose whether you wish to configure a
parameter, skip it, or reset it to its initial default value.
Skipping a parameter will leave it unchanged from its current
value.

Please note that the following parameters are mandatory
and must be configured at least once.
    -> Communication credentials

Changes made will only be applied to the system once all the 
information is entered and verified.

--------------------------------------------------------------

EOF

# Prompt for all requested information
while [ $state != $STATE_FINAL ]; do

    defaultReadValue=""
    echo $state | grep -q "^group_"
    isGroupState=$?

    # Output explanatory prompt if this is a new state
    if [ $state != $stateprev ]; then
     if [ $state = $AUDIT_VERIFY ]; then
     (prompt_$state 2>&1;)| tee -a $AUDIT_LOG_FILE
     else
        prompt_$state
     fi

        # Record that we are in the state
        stateprev=$state

        if [ $isGroupState = 0 ]; then
            if [ $useDefaults = $YES ]; then
                print_default_$state
            else
                print_current_$state
            fi
        fi
    fi
    
    if [ $isGroupState = 0 ]; then
        if [ $useDefaults = $YES ]; then
            defaultReadValue=$RESET_INPUT
        elif [ $fromLogin = $YES ]; then
            defaultReadValue=$ENTER_INPUT
        else
            defaultReadValue=$SKIP_INPUT
        fi
    fi

    # Read the value
    if [ -n "$defaultReadValue" ]; then
        read_$state "$defaultReadValue"
    else
        read_$state
    fi

    # Check if menu has been enabled
    if [ $? -eq $RC_BACK -a $enableMenu == $NO ]; then
        # Request to go back -- Return to previous state.
        goto_valid_state $GOTO_PREV $state
        [ $? -ne 0 ] && exit $RC_ERROR
    else
        # Validate input
        if [ $isGroupState = 0 ]; then
            validate_$state $defaultReadValue
        else
            validate_$state
        fi

        if [ $? -eq $RC_OK ]; then
            # Input is valid -- Move on.
            goto_valid_state $GOTO_NEXT $state
            [ $? -ne 0 ] && exit $RC_ERROR
        fi

        # Check for repeat
        if [ $state = $STATE_FINAL -a "$verify" != "$YES" ]; then
            rm $AUDIT_LOG_FILE
        
        #Load base HA config. existing one
            loadBaseHaConfig $BASE_HACONFIG_FILE

            state=$STATE_INITIAL
        fi
    fi
done

return $RC_OK

}

menuSetupMode() {
# Output greeting
cat <<EOF >&2

--------------------------------------------------------------

CMX Mobile App Server Setup

Please select a configuration option below and enter the 
requested information. You may exit setup at any time by 
typing <Ctrl+C>.

You will be prompted to choose whether you wish to configure a 
parameter, skip it, or reset it to its initial default value.
Skipping a parameter will leave it unchanged from its current
value.

Please note that the following parameters (indicated by *)
are mandatory and must be configured at least once.
    -> None

Changes made will only be applied once all the modifications 
are verified and confirmed.

--------------------------------------------------------------

EOF

    # Menu choice
    MENU_CHOICE=(   "view_config:Display current configuration"
                "group_cred:Communication credentials *"
                "group_sdk:SDK credentials"
                "group_server_port:Server port"
                "group_sdk_port:SDK Server port"
                "group_log_level:Log level"
            "$AUDIT_VERIFY:## Verify and apply changes ##"  )

    #
    # Initialize all change tracking variables to default values
    for menu_state in "${MENU_CHOICE[@]%%:*}"
    do
        eval "input_$menu_state=\$SKIP_INPUT"
    done

    # Initialize NTP related substates
    input_group_ntp_auth="$SKIP_INPUT"
    input_group_timeofday="$SKIP_INPUT"
    
    # Compute the length of the choices
    menulen=$((${#MENU_CHOICE[@]}))

    # Generate prompt string
    printf -v prompt "\nPlease enter your choice [%d - %d]:\t" 1 $menulen
    PS3=$prompt

    # Flag to check if changes need to be applied
    applyFlag=$NO

    # Prompt for menu choices till user decides to quit
    while [ $applyFlag != $YES ]; do
        # Read in user's choice
        printf "\nConfigure CMX Mobile App Server:\n\n";
        select opt in "${MENU_CHOICE[@]##*:}"; do
            printf -v REPLY "%d" $REPLY
            # If user opted to configure CMX Mobile App Server
            if (($REPLY > 0 && $REPLY <= $menulen)); then
                # Get the state corresponding to user's chioce
                state=${MENU_CHOICE[$REPLY - 1]%%:*}
                # Check what state was selected by the user
                if [[ $state != $AUDIT_VERIFY ]] && [[ $state != $STATE_FINAL ]]; then
                    # Set group state and loop end condition accordingly
                    isGroupState=0
                    loopendstate=$AUDIT_VERIFY
                else
                    isGroupState=1
                    loopendstate=$STATE_FINAL
                fi
                # Go down the GOTO_list until we hit the next group state
                while [ $state != $loopendstate ]; do
                    defaultReadValue=""
                    # Output explanatory prompt if this is a new state
                    if [ $state = $AUDIT_VERIFY ]; then
                        (prompt_$state 2>&1;)| tee -a $AUDIT_LOG_FILE
                    else
                        prompt_$state
                    fi
    
                    # Fetch the current value of the state
                    tmp_input_state="input_$state"
    
                    # Skip prompting the group state if its current value is $ENTER_INPUT
                    if [[ ${!tmp_input_state} == $SKIP_INPUT || ${!tmp_input_state} == $RESET_INPUT || $isGroupState != 0 ]]; then
    
                        # If currently in group state
                        if [ $isGroupState = 0 ]; then
                            # Print state if using defaults or otherwise
                            if [ $useDefaults = $YES ]; then
                                print_default_$state
                            else
                                print_current_$state
                            fi
        
                            # Set default read value based on the origin of defaults
                            if [ $useDefaults = $YES ]; then
                                defaultReadValue=$RESET_INPUT
                            elif [ $fromLogin = $YES ]; then
                                defaultReadValue=$ENTER_INPUT
                            else
                                defaultReadValue=$SKIP_INPUT
                            fi
                        fi
                        # Read the input value
                        if [ -n "$defaultReadValue" ]; then
                            read_$state "$defaultReadValue"
                        else
                            read_$state
                        fi

                        # Validate user input
                        if [ $isGroupState = 0 ]; then
                            validate_$state $defaultReadValue
                        else
                            validate_$state
                        fi
                
                    fi
                    # Check if valid response was input 
                    if [ $? -eq $RC_OK ]; then
                        # If the user opted to use defaults, change state value to $ENTER_INPUT
                        # in order to enable prompts for sub-states if user chooses to modify
                        # the state again
                        if [ $isGroupState = 0 -a "${!tmp_input_state}" = "$RESET_INPUT" ]; then
                            # Overwrite state value
                            printf -v input_$state "%s" "$ENTER_INPUT"
                            # Break free fall or else sub-states will prompt for input
                            break;
                        else
                            # Input is valid -- Move on.
                            goto_valid_state $GOTO_NEXT $state
                            [ $? -ne 0 ] && exit $RC_ERROR
                        fi
                    else
                        # Input is invalid -- Repeat the state.
                        continue
                    fi
        
                    # Check for repeat
                    if [ $state = $STATE_FINAL ]; then
                        if [ "$verify" != "$YES" ]; then
                            rm $AUDIT_LOG_FILE
                
                            #Load base HA config. existing one
                            loadBaseHaConfig $BASE_HACONFIG_FILE
                            state=$STATE_INITIAL
                        else
                            # User is happy with the config changes.
                            # End menu and apply changes
                            applyFlag=$YES
                            break;
                        fi
                    fi
    
                    # Check if the next state is going to be a group state
                    echo $state | grep -q "^group_"
                    isGroupState=$?
                    if [ $isGroupState == 0 ]; then
                        # Check for NTP related config states
                        if [[ "group_ntp_auth" != "$state" ]] && [[ "group_timeofday" != "$state" ]]; then
                            break;
                        fi
                    fi
                # End of while loop
                done
            
            # User entered an invalid choice
            else
                echo "Invalid option. Please try again.";
            fi

            # End case
            break;

        # End switch
        done

    # End of while loop
    done

    return $RC_OK
}

############################################################
#
# Main
#
############################################################

# Make sure path is good.
export PATH="$PATH:/bin:/usr/bin:/sbin:/usr/sbin"

# Set terminal characteristics for serial console.
CONSOLETYPE="`consoletype`"
if [ "$CONSOLETYPE" = "serial" ]; then
    export TERM=vt100-nav
    stty erase `tput kbs`
fi

fromLogin="$NO"

# Parse command line parameters.
while [ $# -gt 0 ]; do
    case "$1" in
        -f)  if [ -n "$2" ]; then
                 echo "$2" | egrep -q '^(((\./)|(\.\./))*|(/))?[[:alpha:]]'
                 if [ $? -ne 0 ]; then
                     echo "Invalid file name." >& 2
                     printUsage
                     exit 0
                 else
                     defaultsFilename="$2"; shift;
                 fi
             else
                 echo "Missing file name." >& 2
                 printUsage
                 exit 0
             fi;;
        -l)  fromLogin="$YES";;
        -o)  if [ -n "$2" ]; then
                 echo "$2" | egrep -q '^(((\./)|(\.\./))*|(/))?[[:alpha:]]'
                 if [ $? -ne 0 ]; then
                     echo "Invalid log file name." >& 2
                     printUsage
                     exit 0
                 else
                     logFilename="$2"; shift;
                 fi
             else
                 echo "Missing log file name." >& 2
                 printUsage
                 exit 0
             fi;;
        -*)  printUsage
             exit 0;;
        *)  break;;
    esac
    shift
done

# Set default value for enableMenu
enableMenu="$NO"

# Check if the user would like wizard setup or manual setup
# iff the setup.sh has been launched from the login
if [ "$fromLogin" = "$YES" ]; then
    checkSetupMode
    if [ $? -ne 0 ]; then
        # Check if the user would like the wizard launched at the next
        # login. 
        checkNextLoginSetup
        if [ $? -eq 0 ]; then

            scriptParams="-l "
            if [ -n "$defaultsFilename" -a -f "$defaultsFilename" ]; then
                scriptParams="$scriptParams-f $defaultsFilename "
            fi
            if [ -n "$logFilename" ]; then
                scriptParams="$scriptParams-o $logFilename"
            fi

            usermod -s "/bin/bash $setupFile $scriptParams" root

        else
            removeShellScriptFromBoot
        fi

        # Manual setup
        # Remove the setup shell, and provide a bash prompt
        cat <<EOF >&2

You may run setup at any time by invoking $setupFile

Exiting setup script...
EOF

        # Enable the service, if disabled
        . $setupHome/setup_services
        apply_services

        /bin/bash
        exit 0
    fi
fi


# Load up all of the setup steps
loadSetup
[ $? -ne $RC_OK ] && exit 1

# Load default values
. $setupHome/load_setup_defaults
if [ -n $defaultsFilename ]; then
    loadDefaults $defaultsFilename
fi

# Intialize state
stateprev=$STATE_NONE
state=$STATE_INITIAL

useDefaults=$NO
if [ -n "$defaultsFilename" -a -f "$defaultsFilename" ]; then
    useDefaults=$YES
fi

# Check user preference on menu wizard
checkConfigMode
# Check if config mode returned without errors
if [ $? -eq $RC_OK ]; then
    # Check if user wants menu options enabled
    if [ $enableMenu == $YES ]; then
        # Invoke menu configuration
        menuSetupMode
            [ $? -ne $RC_OK ] && exit $RC_ERROR
    else
        # Invoke fall through configuration
        defaultSetupMode
            [ $? -ne $RC_OK ] && exit $RC_ERROR
    fi
fi

changes_made=`grep "Configuration Changed" $AUDIT_LOG_FILE`
#if [ -n changes_made -a "$changes_made" -eq 0 ]; then
if [ $? != 0 ]; then
    if [ "$fromLogin" = "$YES" ]; then
        cat <<EOF >&2

You may re-run setup at any time by invoking $setupFile

EOF
    fi

    echo "Exiting setup script..."
    if [ "$fromLogin" = "$YES" ]; then
        removeShellScriptFromBoot
        /bin/bash
    fi
    rm -f $AUDIT_LOG_FILE
    exit 0
fi

# Output working message
cat <<EOF >&2

Setup will now attempt to apply the configuration.

EOF

# Move the old setup log file out of the way.
if [ -n "$logFilename" ]; then
    LOG_FILE=$logFilename
else
    LOG_FILE=$setupHome/setup.log
fi
savefn=`find_unused_filename $LOG_FILE`
mv $LOG_FILE $savefn 2> /dev/null

if [ "$fromLogin" = "$YES" ]; then
    # Remove the setup shell
    removeShellScriptFromBoot
fi

# There is no good way to capture the return code from performSetup
# while also teeing the output to a file.  So, if the return code is
# good, echo an easily recognized string to the file, and then check
# the file for that string later to see if the return code was good.
success="***Configuration successful***"
(performSetup 2>&1; [ $? -eq 0 ] && echo "$success") | tee -a $LOG_FILE
fgrep -q "$success" < $LOG_FILE

if [ $? -ne 0 ]; then
   OPSTATUS=FAIL
    cat <<EOF >&2

ERROR: One or more of the requested configurations was not applied.

EOF
else
        apply_resetmse 2>> $setupHome/setup.log
        OPSTATUS=SUCCESS
    cat <<EOF >&2

Exiting setup script...

EOF

fi

begin="-------BEGIN--------"
end="-------END--------"
v3="first"
checkline=-1
while read line
do
if [ "$end" = "$line" ]; then
        checkline=0
fi
if [ $checkline = 1 ]; then
if [ "$v3" = "first" -a "$line" != "" ]; then
v3="$line"
elif [ "$line" != "" ]; then
v3="$v3, $line"
fi
 fi
 #grep -i $end $line
 if [ "$begin" = "$line" ]; then
     checkline=1
 fi
done < $AUDIT_LOG_FILE
rm -f $AUDIT_LOG_FILE

# Without this last sleep, output gets lost.
exit $rc
