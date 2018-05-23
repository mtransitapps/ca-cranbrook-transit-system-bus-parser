package org.mtransit.parser.ca_cranbrook_transit_system_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://bctransit.com/*/footer/open-data
// https://bctransit.com/servlet/bctransit/data/GTFS - Cranbrook
public class CranbrookTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-cranbrook-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CranbrookTransitSystemBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Cranbrook Transit System bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Cranbrook Transit System bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "27"; // Cranbrook Transit System only

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!INCLUDE_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 1: return "0D4C85";
			case 2: return "86C636";
			case 3: return "F18021";
			case 4: return "03A14D";
			case 5: return "FECE0E";
			case 7: return "27A8DD";
			case 14: return "E91A8B";
			case 20: return "AC419C";
			// @formatter:on
			}
			System.out.printf("\nUnexpected route color for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(1L, new RouteTripSpec(1L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walmart", // Walmart Via Tamarack
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown") // Tamarack - Downtown
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170509", // == Eastbound Kootenay St N at Victoria Ave N
								"170427", // <> == Northbound 21st Ave N at Kootenay St N
								"170428", // <> == Eastbound 12th St N at 21st Ave N
								"170429", // <> != Southbound Tamarack Mall Access
								"170538", // <> == Eastbound 12th St N at mall access
								"170531", // <> == Eastbound 12th St N at Kokanee Dr N
								"170414", // <> Southbound Kootenay St N at 12th St N
								"170415", // <> Westbound Kootenay St N at McLeary Cres N
								"170417", // <> Southbound 24th at McLeary Cres N
								"170420", // <> Southbound 24th at 7th
								"170421", // <> Westbound 6th at 23rd
								"170423", // <> Westbound 6th at 21st
								"170425", // <> Northbound Victoria Ave N at 8th St N
								"170427", // <> == Northbound 21st Ave N at Kootenay St N
								"170428", // <> == Eastbound 12th St N at 21st Ave N
								"170538", // <> == Eastbound 12th St N at mall access
								"170531", // <> == Eastbound 12th St N at Kokanee Dr N
								"170413", // == Northbound Kootenay St N at 12th St N
								"170409", // Westbound Cranbrook Mall Access Rd
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"170409", // Westbound Cranbrook Mall Access Rd
								"170407", // == Eastbound Willowbrook Dr at Kokane
								"170410", // != Southbound Willowbrook Dr 1700 Block
								"170405", // != Northbound Kokanee at Kelowna Cres
								"170412", // != Southbound 30th at Mt Fisher Dr
								"170414", // <> Southbound Kootenay St N at 12th St N
								"170415", // <> Westbound Kootenay St N at McLeary Cres N
								"170417", // <> Southbound 24th at McLeary Cres N
								"170420", // <> Southbound 24th at 7th
								"170421", // <> Westbound 6th at 23rd
								"170423", // <> Westbound 6th at 21st
								"170425", // <> Northbound Victoria Ave N at 8th St N
								"170427", // <> Northbound 21st Ave N at Kootenay St N
								"170428", // <> Eastbound 12th St N at 21st Ave N
								"170429", // <> Southbound Tamarack Mall Access
								"170426", // == Southbound 21st Ave N at Kootenay St N
								"170530", // != Southbound Kokanee Dr N at 12th St N
								"170429", // <> Southbound Tamarack Mall Access
								"170426", // == Southbound 21st Ave N at Kootenay St N
								"170508", // == Westbound Kootenay St N at Victoria Ave N
								"170409", // Westbound Cranbrook Mall Access Rd
						})) //
				.compileBothTripSort());
		map2.put(2L, new RouteTripSpec(2L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Highlands", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170524", // ++
								"170474", // Northbound 30th Ave S at 7th St S
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"170474", // Northbound 30th Ave S at 7th St S
								"170464", // ++
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.compileBothTripSort());
		map2.put(3L, new RouteTripSpec(3L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "3rd Ave") // 3rd Ave Via Innes
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"170510", // Eastbound 11th St S at Innes Ave S
								"170490", // ++
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170489", // ++
								"170510", // Eastbound 11th St S at Innes Ave S
						})) //
				.compileBothTripSort());
		map2.put(4L, new RouteTripSpec(4L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Mission Pl", // Slaterville
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170502", // ++
								"570002", // Mission Place
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"570002", // Mission Place
								"170501", // ++
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.compileBothTripSort());
		map2.put(5L, new RouteTripSpec(5L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "College", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170443", // ==
								"170539", // !=
								"170431", // == Westbound College Way
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"170431", // == Westbound College Way
								"170445", // ++
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.compileBothTripSort());
		map2.put(7L, new RouteTripSpec(7L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown", // 11th Ave
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "South") // 7th Ave
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"170002", // Southbound 4th Ave S at Birch Dr
								"170479", // ++
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170514", // ++
								"170002", // Southbound 4th Ave S at Birch Dr
						})) //
				.compileBothTripSort());
		map2.put(14L, new RouteTripSpec(14L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "South") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"170460", // Westbound 20A St S at 14th Ave S
								"170537", // ==
								"170518", // !=
								"170541", // !=
								"170516", // ==
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170461", // ++
								"170460", // Westbound 20A St S at 14th Ave S
						})) //
				.compileBothTripSort());
		map2.put(20L, new RouteTripSpec(20L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "South") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"170002", // Southbound 4th at Birch Dr
								"170484", // ++
								"170545", // Southbound 12th Ave N at Baker St
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"170545", // Southbound 12th Ave N at Baker St
								"170485", // ++
								"170002", // Southbound 4th at Birch Dr
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	private static final String EXCH = "Exch";
	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W){1}(exchange)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCH + "$4";

	private static final Pattern CLEAN_P1 = Pattern.compile("[\\s]*\\([\\s]*");
	private static final String CLEAN_P1_REPLACEMENT = " (";
	private static final Pattern CLEAN_P2 = Pattern.compile("[\\s]*\\)[\\s]*");
	private static final String CLEAN_P2_REPLACEMENT = ") ";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CLEAN_P1.matcher(tripHeadsign).replaceAll(CLEAN_P1_REPLACEMENT);
		tripHeadsign = CLEAN_P2.matcher(tripHeadsign).replaceAll(CLEAN_P2_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		System.out.printf("\nUnexpected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
