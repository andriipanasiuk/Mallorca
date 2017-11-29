package mallorcatour.neural.bot.cuba;

import mallorcatour.neural.core.INeuralInfo;

class TurnNeuralInfo implements INeuralInfo {
private int[] layerSizeArray = new int[] {13, 27, 3};
private double[] weights = new double[] {2466.281020590579, 
-787.5728608183643, -1374.33105995732, 162.07089445018502, -2888.9132172692744, -1295.082485665811, 4390.542092687351, -5607.342885243307, -5383.368191346534, -2116.572681330459, 315.7946311781555, 
-481.5303345922466, 1444.3663706871416, 2073.1819978808517, 1134.8756018089482, 1262.5194408598225, 2965.1247703736153, 697.2840314028791, 5070.672669830903, 1327.0989500877417, 366.26074293777003, 
-277.18620707342933, -28.80683449433924, 242.54656038016722, -3.881562598441708, 1348.1511383011277, 4660.335262301712, 73.96015058065402, -2050.730370586208, 2244.29481676795, -2071.493967388589, 
881.9115801967282, 1594.4470029042634, -4265.7704746658765, -6169.475373108993, -1891.5148178391385, 543.7851679522445, -698.2375823435867, 1443.5502012990473, -183.2469419329161, 971.2492136684814, 
2470.7675879671665, 2839.9946951944744, 1714.7138969690975, 5445.829102085145, 1423.475845907075, 1209.5842084290975, 1505.6276021940478, 453.90505551598704, 144.94020690529035, 417.26302049389835, 
1347.8693733226612, -1263.5046348482704, 741.1092481387877, 2760.631958427964, 2417.704712849247, 1834.0939464652283, 4969.014510249976, 1730.0735002299093, 1126.4861367540327, 1871.777121773247, 
510.1147488445751, 110.17512294293036, 580.623042034643, 1347.9205198957125, 2495.298532045681, 1467.6946251279094, 1600.6123715385993, 3692.06150050435, 1228.058959099209, 6286.121044222264, 
806.4890179985202, 1166.597281575241, 349.3280293690544, 225.35279639766762, 237.14981707609206, -8.544305005904292, 1347.5850278301793, -1547.0691808087727, 957.2396893427657, 3187.530232696552, 
2698.395278004386, 2350.44608118984, 5698.143689634566, 1588.7810839784515, 1809.3450470051748, 2674.3093323700664, 784.9237182607611, 78.06872969919283, 672.4895418828464, 1347.3545797024613, 
874.5665205874146, -483.3043464882362, -1435.7609051079805, 675.2967801258361, -1262.4974194793958, -1284.772729686072, 5450.7617534456995, -4971.304248010275, -4436.218021529035, -1686.8558723436968, 
345.9592995211826, -207.39302701884887, 1904.4904661457529, 3603.1075864990535, -316.5947827785741, -1361.4265510852258, 1813.4533609830605, -2752.1087911721775, 627.8095471990382, 2088.147807333601, 
-4644.294824175927, -5337.088031599392, -1880.4321014669474, 436.1624502518416, -527.0175029464272, 1459.7338803135067, 7215.258907080549, 228.43952539039802, -3834.89812230042, 2073.489995061134, 
322.31030712399735, -629.807548484963, -3958.8746775209097, 1065.8154117755685, -3971.102184556268, -340.1899410651336, 225.23314863051738, -1277.4531331398343, -681.8330172576436, 2948.226210408395, 
649.1628864714427, 523.6621595709958, 2886.1759669741964, -208.07319878147297, 4060.6246342861855, 1111.200868783979, -907.6319751285001, -1924.4176566571018, -588.3478341948162, 330.4674415842227, 
-215.77589594030823, 1346.0491735358478, -6947.169630038383, -257.90181739909616, 4255.586097686339, -1302.7881348384315, 2771.432456669036, 608.8610455612978, 1282.377443137358, 1841.7573375682543, 
6050.768172205055, 1475.1631637037983, -261.63554095173265, 1197.8069507413745, -85.45081279096122, 1499.7004932157688, 453.5270191154964, 835.6816826807365, 2144.707927383941, -82.49283866692082, 
3340.7961296816566, 1752.710368046416, -964.9127543259401, -1375.7047154820511, -480.9901144815848, 272.3769897578922, -1.3422415173287945, 1347.8975778899053, 2929.2061676054714, 635.9550723394851, 
412.60866683222997, 2706.9229541584664, -331.5291615784296, 3820.246827005964, 1271.3370868795184, -1039.4227533536846, -2047.7703193072775, -630.0067290696409, 329.72821384677826, -226.8771140013759, 
1344.883538482869, 2015.4525898989293, 1644.9167551651078, 1519.9471467316062, 3761.5854495597764, 2223.2541000372657, 6184.3930410804505, -1637.1032977398067, 2864.777480147093, 2344.4770518662194, 
855.1673603791567, 80.97533465604872, -38.45025932865035, 354.52557760111574, -9297.57098483874, -1289.9986395070275, 5830.456846479128, -125.20731533728798, 4194.372972112786, 1928.8108163964077, 
1204.1651123655981, 2162.31791208356, 6464.935201335224, 1484.1479705926604, -281.98169420836456, 1737.580481581146, 680.3267421579965, -2405.9232642076545, 360.66793449994907, 2818.103557907976, 
1663.3473557266232, 1640.3098028137022, 3895.680095215907, 2271.7913527611263, 611.6452834466334, 1830.8150090747429, 415.79083973943256, 84.57600736061251, 721.1289948161611, 1347.8828248346254, 
-147.25283154214677, 1086.6931096894534, 2723.6467785856403, 3197.283568377883, 2032.473308133083, 6039.116607540765, 1146.9576228810806, 1617.973218957843, 1899.3088575781962, 595.4115083823918, 
138.02171944636316, 445.1630816850259, 1347.7031352049446, 1503.1151742964403, 297.797512775822, 596.862250299177, 1864.4153573610727, -399.16186446680626, 2782.326643391411, 1931.9300564123669, 
-1402.7935912812604, -1815.947367075161, -640.8177590579819, 285.56591374792833, -33.90747475573102, 1347.2152089582978, 2168.610314389119, 838.9908520725509, 981.0299578479087, 2775.4526953994296, 
286.8053862826931, 4418.293690803899, 1344.4554347849933, -278.3382618613337, -965.5417719609746, -282.0397109078461, 273.76813436690054, -57.75124154921327, 1347.8092326022734, 3158.8510826720376, 
1895.555072165426, 2277.1363596683586, 5102.898588437947, 2174.978361263114, 8417.912661015707, -316.7160682717736, 2459.5102678150515, 1365.3912255441903, 623.6967501239455, 238.3163490739348, 
-2.3734835957613107, 1346.957816843823, 567.02455049848, 1047.3389713389747, 2396.4824548479182, 3368.238180216363, 1730.8133500104082, 5971.982065988099, 943.1935452677578, 1297.3216071318182, 
1279.2097551175189, 413.6485801437146, 178.32102359104488, 315.29412909109914, 1347.5134526508073, -123.00255806870041, 1035.0583546388586, 2645.422932559374, 3126.60011707218, 1930.1328212275773, 
5874.6717186594, 1185.4082629215911, 1475.607087117487, 1746.201505965898, 540.6205890830331, 143.6112135297531, 431.2653242017237, 1347.611810428893, -4172.9010013934485, -194.72404557414507, 
1760.3438304036652, -1545.2416874204093, -810.4421650051456, -87.59257121754226, 3369.2451032733193, -1135.7940784574616, 653.0300721655959, 60.00069075299335, -77.58536348727394, 708.8598382578995, 
715.1644702608343, 4476.727930997005, 709.6872485667195, -973.0020027487542, 2754.0975889758047, -2685.9329348145643, 2945.1028056366954, 1530.150148658511, -3647.873055535614, -4716.863073142796, 
-1635.7107099341756, 512.4411499496906, -574.1395239905786, 1349.4055056497261, 4190.02187379222, 2168.500926697663, 1467.008272467476, 5506.705163350055, 2761.757042892409, 8167.466579297288, 
-2560.6843594864777, 3424.357375048696, 2346.148914239125, 965.0938318315156, 156.9556931750247, -276.06874876095736, 592.5926417080459, -8.544033904599399, -2.1579319411261846, -3.2759635162525664, 
-0.10228008773577536, 0.21879839403824977, -1.2589707308792244, 0.9471202851332294, -10.63632800755122, -2.898066362074401, -1.6485760034901624, -2.8700560215141873, 7.182594388756797, -2.894051655566315, 
-3.20840645615851, 4.749723706315375, 6.276958046664482, -0.0855657004359116, 0.47723734967648673, -3.411484209240192, -2.5212250965383167, 0.718275239374955, 0.19545113616534188, 0.34502132961410586, 
-0.6368096382209762, -4.700243487662969, 4.89720970121626, -0.51109967687441, 0.8077442807288822, -1.1256035307060062, 0.9806886340855286, -0.4914688472156179, -0.04846289769259297, -1.5507798193812312, 
-0.24502550451676666, 1.2412805421652984, 0.029493226941668487, -1.4228352752446758, -0.39805497662116396, 2.121030235581054, -0.08663939623682154, -0.4092563627161938, -0.49596423613816715, 4.503312449784449, 
0.6160228802495733, -0.429789970000446, 0.18647405647946286, -0.5989554874821489, -1.8140042448563347, -0.4847223818215848, -0.4398535075429417, 1.00183931603733, -0.7573138312721331, -1.396225135549844, 
-0.18256515968446962, -0.7740877760397548, -0.13868823947908973, 3.2040246306067215, -0.921129381960165, -1.2371560759091291, -0.04114021911286851, -1.592810112950283, -1.1107476312548932, 1.491315322193337, 
1.479296094392788, 1.2551904875435107, -2.253279376947126, 0.3378691382691968, 1.0786092737941362, 1.3166736536697774, -4.283324529846359, -1.5580702837037155, -0.8501513193202076, 0.44840934638012825, 
0.37029945107211043, 0.5805465698513872, -0.24644435363350728, -0.7104110605343238, -0.8279699446801636, 4.4573508217847575, 2.30502284856939, -1.2854382208166655};
public int[] getLayerSizes() {
return layerSizeArray;
}
public double[] getWeights() {
return weights;
}
}