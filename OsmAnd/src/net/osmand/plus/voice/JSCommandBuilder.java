package net.osmand.plus.voice;

import net.osmand.PlatformUtil;

import org.apache.commons.logging.Log;
import org.json.JSONObject;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSCommandBuilder extends CommandBuilder {

    private static final Log log = PlatformUtil.getLog(JSCommandBuilder.class);

    private Context jsContext;
    private List<String> listStruct = new ArrayList<>();
    ScriptableObject jsScope;

    public JSCommandBuilder(CommandPlayer commandPlayer) {
        super(commandPlayer);
    }

    public void setJSContext(String path) {
        String script = readFileContents(path);
        jsContext = Context.enter();
        jsContext.setOptimizationLevel(-1);
        jsScope = jsContext.initStandardObjects();
        jsContext.evaluateString(jsScope, script, "JS", 1, null);
    }

    private Object convertStreetName(Map<String, String> streetName) {
        return NativeJSON.parse(jsContext, jsScope, new JSONObject(streetName).toString(), new NullCallable());
    }


    private String readFileContents(String path) {
        FileInputStream fis = null;
        StringBuilder fileContent = new StringBuilder("");
        try {
            fis = new FileInputStream(new File(path));
            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return fileContent.toString();
    }

    public void setParameters(String metricCons, boolean tts) {
//        jsContext.property("setMode").toFunction().call(jsContext, tts);
        Object obj = jsScope.get("setMode", jsScope);

        if (obj instanceof Function) {
            Function jsFunction = (Function) obj;
            jsFunction.call(jsContext, jsScope, jsScope, new Object[]{metricCons});
            jsFunction.call(jsContext, jsScope, jsScope, new Object[]{tts});
        }
    }

    private JSCommandBuilder addCommand(String name, Object... args){
        //  TODO add JSCore
        Object obj = jsScope.get(name);
        if (obj instanceof Function) {
            Function jsFunction = (Function) obj;
            Object jsResult = jsFunction.call(jsContext, jsScope, jsScope, args);
            listStruct.add(Context.toString(jsResult));
        }
        return this;
    }

    public JSCommandBuilder goAhead(){
        return goAhead(-1, new HashMap<String, String>());
    }

    public JSCommandBuilder goAhead(double dist, Map<String, String> streetName){
        return addCommand(C_GO_AHEAD, dist, convertStreetName(streetName));
    }

    public JSCommandBuilder makeUTwp(){
        return makeUT(new HashMap<String, String>());
    }

    public JSCommandBuilder makeUT(Map<String, String> streetName){
        return addCommand(C_MAKE_UT, convertStreetName(streetName));
    }
    @Override
    public JSCommandBuilder speedAlarm(int maxSpeed, float speed){
        return addCommand(C_SPEAD_ALARM, maxSpeed, speed);
    }
    @Override
    public JSCommandBuilder attention(String type){
        return addCommand(C_ATTENTION, type);
    }
    @Override
    public JSCommandBuilder offRoute(double dist){
        return addCommand(C_OFF_ROUTE, dist);
    }
    @Override
    public CommandBuilder backOnRoute(){
        return addCommand(C_BACK_ON_ROUTE);
    }

    public JSCommandBuilder makeUT(double dist, Map<String,String> streetName){
        return addCommand(C_MAKE_UT, dist, convertStreetName(streetName));
    }

    public JSCommandBuilder prepareMakeUT(double dist, Map<String, String> streetName){
        return addCommand(C_PREPARE_MAKE_UT, dist, convertStreetName(streetName));
    }


    public JSCommandBuilder turn(String param, Map<String, String> streetName) {
        return addCommand(C_TURN, param, convertStreetName(streetName));
    }

    public JSCommandBuilder turn(String param, double dist, Map<String, String> streetName){
        return addCommand(C_TURN, param, dist, convertStreetName(streetName));
    }

    /**
     *
     * @param param A_LEFT, A_RIGHT, ...
     * @param dist
     * @return
     */
    public JSCommandBuilder prepareTurn(String param, double dist, Map<String, String> streetName){
        return addCommand(C_PREPARE_TURN, param, dist, convertStreetName(streetName));
    }

    public JSCommandBuilder prepareRoundAbout(double dist, int exit, Map<String, String> streetName){
        return addCommand(C_PREPARE_ROUNDABOUT, dist, exit, convertStreetName(streetName));
    }

    public JSCommandBuilder roundAbout(double dist, double angle, int exit, Map<String, String> streetName){
        return addCommand(C_ROUNDABOUT, dist, angle, exit, convertStreetName(streetName));
    }

    public JSCommandBuilder roundAbout(double angle, int exit, Map<String, String> streetName) {
        return roundAbout(-1, angle, exit, streetName);
    }
    @Override
    public JSCommandBuilder andArriveAtDestination(String name){
        return addCommand(C_AND_ARRIVE_DESTINATION, name);
    }
    @Override
    public JSCommandBuilder arrivedAtDestination(String name){
        return addCommand(C_REACHED_DESTINATION, name);
    }
    @Override
    public JSCommandBuilder andArriveAtIntermediatePoint(String name){
        return addCommand(C_AND_ARRIVE_INTERMEDIATE, name);
    }
    @Override
    public JSCommandBuilder arrivedAtIntermediatePoint(String name) {
        return addCommand(C_REACHED_INTERMEDIATE, name);
    }
    @Override
    public JSCommandBuilder andArriveAtWayPoint(String name){
        return addCommand(C_AND_ARRIVE_WAYPOINT, name);
    }
    @Override
    public JSCommandBuilder arrivedAtWayPoint(String name) {
        return addCommand(C_REACHED_WAYPOINT, name);
    }
    @Override
    public JSCommandBuilder andArriveAtFavorite(String name) {
        return addCommand(C_AND_ARRIVE_FAVORITE, name);
    }
    @Override
    public JSCommandBuilder arrivedAtFavorite(String name) {
        return addCommand(C_REACHED_FAVORITE, name);
    }
    @Override
    public JSCommandBuilder andArriveAtPoi(String name) {
        return addCommand(C_AND_ARRIVE_POI_WAYPOINT, name);
    }
    @Override
    public JSCommandBuilder arrivedAtPoi(String name) {
        return addCommand(C_REACHED_POI, name);
    }

    public JSCommandBuilder bearLeft(Map<String,String> streetName){
        return addCommand(C_BEAR_LEFT, convertStreetName(streetName));
    }

    public JSCommandBuilder bearRight(Map<String, String> streetName){
        return addCommand(C_BEAR_RIGHT, convertStreetName(streetName));
    }

    @Override
    public JSCommandBuilder then(){
        return addCommand(C_THEN);
    }

    @Override
    public JSCommandBuilder gpsLocationLost() {
        return addCommand(C_LOCATION_LOST);
    }

    @Override
    public JSCommandBuilder gpsLocationRecover() {
        return addCommand(C_LOCATION_RECOVERED);
    }

    @Override
    public JSCommandBuilder newRouteCalculated(double dist, int time){
        return addCommand(C_ROUTE_NEW_CALC, dist, time);
    }

    @Override
    public JSCommandBuilder routeRecalculated(double dist, int time){
        return addCommand(C_ROUTE_RECALC, dist, time);
    }

    @Override
    public void play(){
        this.commandPlayer.playCommands(this);
    }

    @Override
    protected List<String> execute(){
        alreadyExecuted = true;
        return listStruct;
    }

    public class NullCallable implements Callable
    {
        @Override
        public Object call(Context context, Scriptable scope, Scriptable holdable, Object[] objects)
        {
            return objects[1];
        }
    }
}
