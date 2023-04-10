package com.streambus.commonmodule.download;

import android.text.TextUtils;

import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SimpleCall;
import com.streambus.commonmodule.Constants;
import com.streambus.requestapi.OkHttpHelper;
import com.streambus.requestapi.RALog;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.CompositeException;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/7/30
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class M3u8DownloadUtils {
    private static final String TAG = "M3u8DownloadUtils";
    private static final long CHEAT_MAP[] = {
            1324352448,1223809089,1330546167,1661808397,1951299482,44434263,1097133768,1709935880,26229807,1332986271,
            1781765587,1622607911,715769807,917917648,2023448086,1581759894,198089965,1581458516,1962098265,946993862,
            81616491,1246287151,1019666877,51136867,825416061,1576386651,265507113,234597757,1435594131,981567753,
            412344087,960249433,1925649567,755615052,1977040367,1915427426,185526529,475695649,1312535388,1339953534,
            1800048098,946817327,815077798,368334257,1864734976,691042236,1950094151,2062824941,125017104,1764708768,
            862335155,206633596,863512272,1882002032,257770463,1688928333,1310905035,523277577,1923526090,599015519,
            1504845330,188386529,1559264952,1283011249,944001581,1388821671,1050955027,1129528110,1864517321,216006768,
            321997996,1517081771,1162824095,1137075794,1885416028,880075423,1828118030,1688026531,795416716,1953135135,
            1305251651,1657751872,12285083,21280275,1392270256,270055546,1710208608,555691644,793333123,1486251050,
            1154707163,150694805,1674637579,566488467,1433706055,471155512,1955310139,337177434,1600683622,1672343812,
            553184202,1922681619,1041941935,1716008298,912273765,779874315,448600073,592908148,320417198,1244016790,
            398559635,1625668849,754285014,410844718,1646949125,2146555270,680900264,1209674085,554763266,1474233388,
            548441488,1709470429,1624928193,75595419,128475249,911150600,546750932,2083785388,1248328035,2147434554,
            1608645552,1801512237,1922632525,503103839,1370036887,687422643,1282978154,1818636961,1280330791,1603395352,
            915170103,1678890426,1081580553,1669455117,2089735144,581046030,1668526739,623151760,1790720116,75806358,
            2097385148,191677956,1785276787,1574829694,267273375,1913752036,338496646,814024307,1850053776,1586824681,
            813975214,1311215680,1240853271,589124091,1814319519,463406510,1276546734,949814025,134559823,409393877,
            405725729,1049729926,2088284303,1487306283,571701395,2030535799,2068352313,92744487,506203912,1711588781,
            168550845,456105412,1903266737,1953827632,2030935106,23056465,1720096021,221948105,837080772,1422666149,
            1808772786,1651055986,586398182,902142409,92696430,253234053,1365548920,1369243164,1203048079,1500108743,
            1778637042,1608773808,402355022,1719437697,948596443,974056417,1602489849,869465109,1066800904,2108693761,
            433570242,1235351749,417315525,189353332,1041695734,300766984,212409797,614308107,522715089,1049490569,
            2036974256,184004227,553062908,475888790,1086146637,645759338,729122844,304211909,2015002502,1932170923,
            1804320652,1646155896,1393461083,59192026,1218109946,194573879,1033248444,673116147,1064038988,2100049348,
            634326260,1497609230,1187917450,1051641785,1686962562,82129536,1352408769,1899372359,696437643,1875123858,
            801379281,585928251,2059128086,1354442189,1061817042,997791075,2000201527,1790939886,1302002984,1867720381,
            1575627161,958839988,1366392630,821604596,1018032015,437018928,1016178475,2051280459,1110135075,2080217463,
            2003846159,1744461335,1430343046,1044279961,648619472,969821960,1126409497,2001028242,721710672,1822847140,
            1728668452,1523089953,261291744,1640312890,730048494,1323108786,490620317,582766373,966565024,1792623301,
            303003106,394708537,603979642,1669395736,1216313133,1622011657,2106414664,85007961,1525808468,1069066091,
            17741776,1382170979,666043778,1448084822,278967293,1314663251,270423135,1405376790,1168207845,992133807,
            1080740283,749392649,367740112,1342032027,242221892,1097788606,517657165,732842209,1680554979,1484222189,
            377981863,1983558085,1878930726,981961505,1505470174,947760211,456489514,1464401190,1032768172,1982297982,
            385983634,1050509949,1216985313,1052027412,351111123,1495952606,219207015,621534258,753845749,1387414860,
            1613668065,1834586032,2136807510,1981408177,1029134411,231545754,931713135,1546791576,964387963,464784466,
            883530117,1342369826,300858904,614977195,176847683,1806329078,1562737406,633337197,1123246620,448021931,
            468151531,1509230254,1498531880,1685136845,413774019,1849643003,1033605803,632981034,323693614,1787451552,
            2020395895,1937361679,1474553936,2009719757,1771286209,356204699,93781863,555515696,1902996275,1058169826,
            1020300163,639042744,253056005,1321159067,1254019939,429903688,980004497,669273698,1063240886,2103251117,
            1117295629,1531392417,1464997724,468343861,1069045614,1878771743,170503216,2102651418,364269129,494196830,
            1742619322,237181376,284074862,1069689611,99417485,2055361071,1425894310,193199348,463393119,1181406938,
            1251369175,1483693282,1820449682,1504425180,657368701,926985974,1934328868,1637373198,1596259672,850086106,
            1593140668,566071653,233994876,910654744,1034415514,1303040490,641942839,1204918730,1258208260,1006211968,
            1699115561,853343935,1243393345,1983190423,1923033546,1342810830,1891067846,1201444208,1536010179,206977317,
            235367498,639895706,1690670600,2055817181,2144320886,200555653,835319507,1931166106,1837928852,284095531,
            633768565,1283585872,850167184,867763441,46756968,1884582698,23320283,688699807,942017780,1281528544,
            1694911775,493649693,2134872479,790821472,329356468,1910422377,2133632303,72940666,964382937,1522158834,
            279917984,1199750436,14570892,1970588584,1108083969,11408130,23660589,1943403476,1942574236,1861589441,
            80015359,428859153,997691665,930182543,1296622594,1044448633,667281593,1319942878,1733148440,1609299373,
            453987774,1280576568,2102949067,441376605,2071398040,284821887,204315334,2057546695,357762554,1168698271,
            1432221881,637680538,220965059,1446792773,460785474,1329049028,1458200903,484446063,1124968856,1253291492,
            198551857,1204984215,1682150645,1196243522,2135166758,831289592,93208508,654964703,3748822,1826356948,
            116780429,457736596,959449868,72245848,899113201,883364261,357067735,1103428535,793427308,714830289,
            124643158,78165542,1352510827,345608218,1524958315,1813296301,1674657246,835675571,150258717,652142455,
            2088967063,348810574,1857126670,1623634060,1545054096,1844809781,307440004,1638262604,352290836,311188826,
            1317135905,469071265,768925422,129102125,541317113,1668038623,1012466386,898384849,623983510,1805893695,
            1613215138,748626669,1884059237,818242318,1094234887,1261533904,484054971,621408485,2097209475,634313688,
            1273550940,2038692890,983124262,983193963,1514843303,380694711,680520096,1822283307,2018957315,1032810932,
            2133472134,1188609572,1501882198,754913908,1317711698,2043199311,275468884,182694436,794100512,899452394,
            1988588131,259832003,1648079063,1725163720,1078074321,594830302,839213977,1562129292,1216238788,788939804,
            48959333,342306080,680149047,1032083595,1325500043,47508702,1412778306,2006020139,1869792009,1284251974,
            891347424,1855780495,325377898,245745974,463210756,1643089596,141461637,738679640,1825784033,935562150,
            1638132034,1666888516,1195394153,1138727450,1244568589,125984826,1733557752,2083782566,1688114118,802312892,
            725238722,1737073451,1144618973,1405387769,621673399,322635368,1452896471,2034451705,181171860,1175204833,
            1171220031,1072519284,883501680,1496597930,1318265258,1346712436,992203878,1459726895,2085392076,670504263,
            247805397,1576040463,189909132,1443199550,567284265,1434477721,1569184376,153358369,1370776639,1109814847,
            955671262,2096015361,699404650,2100290235,1353919483,1321078049,275441955,659332306,1208046107,456613815,
            1834537139,231782490,1529133099,570555172,1728380420,699914709,1917267608,573100651,12157957,1855176037,
            1243604914,259963354,1283732852,1433514046,1703162905,1851017117,720508119,1124863633,2004375486,2091284758,
            87194832,812563100,2039816472,786599483,765369687,1246252307,2107677532,1040811643,1905584613,1168239991,
            1497425458,1592638105,1400022482,879074910,15709629,980919254,1578989619,1932977237,1554019905,1591147576,
            1640669626,650141172,1851110931,776918830,2083655218,1406790188,480452299,656679690,384170173,337344138,
            600480800,471365006,1149907238,492813624,1257964489,1915276926,1739065931,1218158373,808604921,1497166897,
            238914717,158546731,942321354,1638937199,1037621641,958030983,472372805,469127613,743524572,2026392711,
            2060275189,236710551,529050235,1763902472,1013629381,465221805,1023209012,1494081681,1121901495,1407379186,
            1831425819,1722382296,1878744192,833849409,67712272,989225033,601642687,1806778204,59899758,1410247608,
            1156461453,298814475,1568794340,2098782807,1937751674,458932333,909330142,262640832,928059946,1652854714,
            141549895,840851488,1889565265,670600130,457270312,755710999,1135821935,1480479325,102309032,110239783,
            740374863,1933734851,1832622079,471635407,620100612,1900334351,1460860440,1221743300,1559628907,1520760198,
            484507260,568606712,1819574674,2053301600,519905871,1609842700,364750286,1429236013,1872483532,1292810232,
            934607080,2014033427,2133661720,676688697,537149909,443448385,1432399696,1672971845,1923927710,1534708728,
            1783211628,516818925,1320959931,1468350059,988454332,1941060544,1221200762,301831124,1015320196,633346022,
            1822591322,1499827456,1201952734,1494682348,1405645409,1721858606,957041401,1770395695,1003610971,682041285,
            915722279,1938218051,548591065,901900352,467423101,1085740974,1345348737,1899822797,611229171,1121792799,
            1287047878,246957151,1638611724,460524161,1715307210,479582408,254101057,789024325,781413532,1269421253,
            1422370347,456521206,621765062,476839433,1951203555,2027410471,51214391,760761308,1650322518,1054825363,
            1442802593,418561149,845559766,1991393658,1320461501,1312982867,929650985,518326590,1065322017,1540880156,
            1640119389,204886247,1787837308,1131247465,665410408,1355660870,1610829873,919511466,2144685195,244759757,
            41449071,1419571894,701280964,663214133,1896411328,505000871,543140956,1947625719,1265762179,45979826,
            854967434,561081124,464540976,1700527201,404991135,1785002477,866026420,1334642120,155845420,1931348437,
            728038628,1795964809,2136234684,368392288,779728627,654161445,1724053159,243074852,1573672911,1721254706,
            487834610,1615121982,993342953,1189115574,130852468,742270633,1694116445,673993424,542412704,812394976,
            719973251,1397380139,1373476100,1184514227,950423692,1778467235,822033056,1816450112,965625707,977878476,
            1600314902,1693664336,626359638,1589065938,2062056624,1406088265,95743735,1638626135,1649163117,1669416646,
            1212397194,2136997727,1137054981,58256499,1178629653,1267907449,800527132,725262450,1941900873,1342939836,
            1537657426,514390476,592836327,763649879,1698904703,1543260019,394633466,373454112,1212226484,1360259174,
            1351332588,665057738,906439862,1977692226,106640028,821012838,1236296843,202383764,312155326,737976313,
            1871800410,1524552520,727490392,861371743,1582809019,1906120046,2129279192,235852503,483898848,1923696418,
            1578792339,2021556275,290603246,24145019,637722506,1989507950,1567405038,1032355972,215478414,632147874,
            245131498,1566811002,1297205612,1151571360,1397019581,1403845641,1972584199,485832776,1606229405,137255877
    };

    private static final Interceptor sLoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            RALog.d(TAG, String.format("request   url=%s\nrequest headers:\n%s", request.url().toString(), request.headers().toString()));
            Response response = chain.proceed(request);
            RALog.d(TAG, String.format("response code=%d  url=%s\nresponse headers:\n%s", response.code(), response.request().url().toString(), response.headers().toString()));
            return response;
        }
    };
    private static final OkHttpClient sOkHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(sLoggingInterceptor)
            .build();


    static Map<String, String> sDomainMapping;

    public static SimpleCall<List<Map.Entry<String, String>>> newParsM3u8Call(File saveDir, String url, int protocol) {
        return new SimpleCall<List<Map.Entry<String, String>>>() {
            private volatile boolean isCancel;
            private SimpleCall call;

            @Override
            public void cancel() throws Exception {
                synchronized (this) {
                    isCancel = true;
                    if (call != null) {
                        call.cancel();
                    }
                }
            }

            @Override
            public List<Map.Entry<String, String>> call() throws Exception {
                return loadParsM3u8(saveDir, url);
            }

            private List<Map.Entry<String, String>> loadParsM3u8(File saveDir, String url) throws Exception {
                SLog.d(TAG, "loadParsM3u8-->" + url);
                List<Map.Entry<String, String>> downloadTsList = new ArrayList<>();
                int index = url.lastIndexOf("/");
                String baseUrl = url.substring(0, index + 1);
                String path = url.substring(index);
                File downFile = new File(saveDir, path + "_down");
                synchronized (this) {
                    if (isCancel) {
                        throw new CancellationException("newParsM3u8Call");
                    }
                    call = newDownloadCall(downFile, url, protocol, true);
                }
                if (!((SimpleCall<Boolean>) call).call()) {
                    throw new IllegalStateException("downloadM3u8 failed，url=>" + url);
                }
                File temFile = new File(saveDir, path + "_tem");
                BufferedWriter writer = new BufferedWriter(new FileWriter(temFile));
                BufferedReader reader = new BufferedReader(new FileReader(downFile));
                String line;
                while (!isCancel && (line = reader.readLine()) != null) {
                    String uri, key;
                    if (line.startsWith("#EXT-X-KEY:")) {//.key
                        int beg = line.indexOf("URI=");
                        if (beg != -1) {
                            uri = checkUri(line.substring(beg + "URI=".length()));
                            key = relativePath(uri);
                            synchronized (this) {
                                if (isCancel) {
                                    throw new CancellationException("newParsM3u8Call");
                                }
                                call = newDownloadKeyCall(new File(saveDir, key), generateUrl(baseUrl, uri), protocol, true);
                            }
                            if (!((SimpleCall<Boolean>) call).call()) {
                                throw new IllegalStateException("downloadKeyFile failed，url=>" + url);
                            }
                            writer.write(line.replace(uri, key));
                            writer.newLine();
                            continue;
                        }
                    }
                    if (line.startsWith("#EXTINF:")) {//.ts
                        writer.write(line);
                        writer.newLine();
                        uri = checkUri(reader.readLine());
                        key = relativePath(uri);
                        int beg = uri.indexOf("_yoos__");
                        int end = uri.lastIndexOf(".ts");
                        if (beg != -1 && end != -1) {
                            String subNum = uri.substring(beg + "_yoos__".length(), end);
                            if (subNum.length() > 9) {
                                subNum = subNum.substring(subNum.length() - 9);
                            }
                            long value = CHEAT_MAP[Integer.valueOf(subNum).intValue() % 1000];
                            uri = uri.substring(0, beg) + "_hxsz_" + value + "_" + uri.substring(beg + "_yoos__".length());
                        }
                        downloadTsList.add(new AbstractMap.SimpleEntry<>(key, generateUrl(baseUrl, uri)));
                        writer.write(key);
                        writer.newLine();
                        continue;
                    }

                    //下载相关的m3u8
                    if (line.startsWith("#EXT-X-STREAM-INF:")) {
                        writer.write(line);
                        writer.newLine();
                        uri = checkUri(reader.readLine());
                        key = relativePath(uri);
                        synchronized (this) {
                            if (isCancel) {
                                throw new CancellationException("newParsM3u8Call");
                            }
                            call = newParsM3u8Call(saveDir, generateUrl(baseUrl, uri), protocol);
                        }
                        downloadTsList.addAll(((SimpleCall<List<Map.Entry<String, String>>>) call).call());
                        writer.write(key);
                        writer.newLine();
                        continue;
                    }
                    if (line.startsWith("#EXT-X-MEDIA:")) {
                        int beg = line.indexOf("URI=");
                        if (beg != -1) {
                            uri = checkUri(line.substring(beg + "URI=".length()));
                            key = relativePath(uri);
                            synchronized (this) {
                                if (isCancel) {
                                    throw new CancellationException("newParsM3u8Call");
                                }
                                call = newParsM3u8Call(saveDir, generateUrl(baseUrl, uri), protocol);
                            }
                            downloadTsList.addAll(((SimpleCall<List<Map.Entry<String, String>>>) call).call());
                            writer.write(line.replace(uri, key));
                            writer.newLine();
                            continue;
                        }
                    }
                    writer.write(line);
                    writer.newLine();
                }
                reader.close();
                writer.close();
                downFile.delete();
                synchronized (this) {
                    if (isCancel) {
                        throw new CancellationException("newParsM3u8Call");
                    }
                    temFile.renameTo(new File(saveDir, path));
                    return downloadTsList;
                }
            }
        };
    }

    private static String relativePath(String uri) {
        int beg = uri.lastIndexOf("/");
        if (beg != -1) {
            uri = uri.substring(beg + 1);
        }
        return uri;
    }

    private static String checkUri(String uri) {
        if (uri.startsWith("\"")) {
            uri = uri.substring(1, uri.indexOf("\"", 1));
        }
        return uri;
    }

    private static String generateUrl(String baseUrl, String uri) {
        return uri.lastIndexOf("/") != -1 ? uri : baseUrl + uri;
    }

    private static SimpleCall<Boolean> newDownloadKeyCall(File file, String url, int protocol, boolean useMapping) {
        return new SimpleCall<Boolean>() {
            private volatile boolean isCancel;
            private Call call;
            @Override
            public void cancel() throws Exception {
                synchronized (this) {
                    isCancel = true;
                    if (call != null) {
                        call.cancel();
                    }
                }
            }
            @Override
            public Boolean call() throws Exception {
                return downloadFile(file, url);
            }

            private boolean downloadFile(File file, String url) {
                if (file.exists()) {
                    SLog.d(TAG, "downloadFile exists file=>" + file.getAbsolutePath());
                    return true;
                }
                if (useMapping) {
                    int index = url.indexOf("://") + 3;
                    String host = url.substring(index, url.indexOf("/", index));
                    String ip = sDomainMapping.get(host);
                    if (!TextUtils.isEmpty(ip)) {
                        url = url.replace(host, ip);
                    }
                }

                SLog.d(TAG,"downloadFile  file=>" + file.getAbsolutePath() + "   url=>" + url);
                int retryCount = 0;
                while (!isCancel && retryCount++ < 3) {
                    Response response = null;
                    FileOutputStream ops = null;
                    try {
                        HashMap<String, String> header = new HashMap<>();
                        String key = OkHttpHelper.getPlayKey(Constants.generatePlayUrl(url, protocol), header);
                        if (key == null) {
                            throw new IllegalStateException("getPlayKey key is NULL");
                        }
                        Request.Builder builder = new Request.Builder().url(url + key);
                        for (Map.Entry<String, String> entry : header.entrySet()) {
                            builder.header(entry.getKey(), entry.getValue());
                        }
                        if (!header.containsKey("User-Agent")) {
                            builder.header("User-Agent", "MavisAgent/4.1");
                        }
                        builder.header("FILE", "FILE");
                        synchronized (this) {
                            if (isCancel) {
                                throw new CancellationException("newDownloadCall");
                            }
                            call = sOkHttpClient.newCall(builder.build());
                        }
                        response = call.execute();
                        if (!response.isSuccessful()) {
                            throw new IllegalStateException("http response code=>" + response.code());
                        }
                        String stringKey = "zsxh" + response.body().string();
                        ops = new FileOutputStream(file);
                        ops.write(stringKey.getBytes());
                        ops.flush();
                        ops.close();
                        ops = null;
                        return true;
                    } catch (Exception e) {
                        SLog.w(TAG, "downloadFile request Exception", e);
                    } finally {
                        if (ops != null) {
                            try {
                                ops.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (response != null) {
                            response.close();
                        }
                    }
                }
                return false;
            }
        };
    }


    public static SimpleCall<Boolean> newDownloadCall(File file, String url, int protocol, boolean useMapping) {
        return new SimpleCall<Boolean>() {
            private volatile boolean isCancel;
            private Call call;

            @Override
            public void cancel() throws Exception {
                synchronized (this) {
                    isCancel = true;
                    if (call != null) {
                        call.cancel();
                    }
                }
            }

            @Override
            public Boolean call() throws Exception {
                return downloadFile(file, url);
            }

            private boolean downloadFile(File file, String url) throws Exception{
                if (file.exists()) {
                    SLog.d(TAG, "downloadFile exists file=>" + file.getAbsolutePath());
                    return true;
                }
                if (useMapping) {
                    int index = url.indexOf("://") + 3;
                    String host = url.substring(index, url.indexOf("/", index));
                    String ip = sDomainMapping.get(host);
                    if (!TextUtils.isEmpty(ip)) {
                        url = url.replace(host, ip);
                    }
                }
                SLog.d(TAG, "downloadFile  file=>" + file.getAbsolutePath() + "   url=>" + url);
                int retryCount = 0;
                ArrayList<Exception> exceptions = new ArrayList<>();
                while (!isCancel && retryCount++ < 3) {
                    Response response = null;
                    BufferedOutputStream ops = null;
                    try {
                        HashMap<String, String> header = new HashMap<>();
                        String key = OkHttpHelper.getPlayKey(Constants.generatePlayUrl(url, protocol), header);
                        if (key == null) {
                            throw new IllegalStateException("getPlayKey key is NULL");
                        }
                        Request.Builder builder = new Request.Builder().url(url + key);
                        for (Map.Entry<String, String> entry : header.entrySet()) {
                            builder.header(entry.getKey(), entry.getValue());
                        }
                        if (!header.containsKey("User-Agent")) {
                            builder.header("User-Agent", "MavisAgent/4.1");
                        }
                        builder.header("FILE", "FILE");
                        synchronized (this) {
                            if (isCancel) {
                                throw new CancellationException("newDownloadCall");
                            }
                            call = sOkHttpClient.newCall(builder.build());
                        }
                        response = call.execute();
                        if (!response.isSuccessful()) {
                            throw new IllegalStateException("http response code=>" + response.code());
                        }
                        InputStream ips = response.body().byteStream();
                        File downingFile = new File(file.getAbsolutePath() + "_downing");
                        ops = new BufferedOutputStream(new FileOutputStream(downingFile));
                        int len;
                        byte[] buff = new byte[1024];
                        while ((len = ips.read(buff)) != -1) {
                            ops.write(buff, 0, len);
                        }
                        ips.close();
                        ops.flush();
                        ops.close();
                        ops = null;
                        downingFile.renameTo(file);
                        return true;
                    } catch (Exception e) {
                        SLog.w(TAG, "downloadFile request Exception", e);
                        exceptions.add(e);
                    } finally {
                        if (ops != null) {
                            try {
                                ops.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (response != null) {
                            response.close();
                        }
                    }
                }
                throw new CompositeException(exceptions);
            }
        };
    }
}
