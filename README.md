# Dike-ED
Discrete-event simulator for medical emergency department resource allocation.

An ED is supported with resources such as doctors, nurses, wardies, xray scanning and pathology labs. The queuing size can be defined as well, along with the number of ED beds. Also, the mean arrivals per hour and the total number of simulating hours can be entered by the user. Results depict resource utilisation percentages and the average delay time inside the ED of incoming patients.

Experimental release, developed in Oracle Java 21.


<pre>
Copyright (C) 2021-2023 by Constantine Kyriakopoulos, zfox@users.sourceforge.net
Version: 0.4.0
License: GNU GPL Version 2
</pre>

## Changelog

<pre>
0.4.0      2023-07-22    A GUI to run the simulator is created. Required 
                         is the forms_rt.jar lib from IntelliJ IDEA IDE.
0.3.5      2023-07-11    Introduction of a simple algorithm plugin API.
                         Temporarily removed the json-simple dependency.
                         Code clean-up.
0.3.0      2023-03-12    Initial public release.
</pre>
