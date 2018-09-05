M83				;Set Extruder to Relative moves
M139 D			;Set & heat first layer Bed temp.

Macro:Home_all_Axis_in_sequence

M190			;Wait for Bed to get to temp.
M103 S			;Set & heat first layer nozzle temp.
M109			;Wait for Nozzle to get to temp.
M170 D			;Set Ambient temp.

Macro:Level_Gantry_(2-points)

Macro:7_point_Bed_probing-Set_Washout

Macro:Short_Purge

;Prime
G1 D0.5 F400
M129			;Head LED on