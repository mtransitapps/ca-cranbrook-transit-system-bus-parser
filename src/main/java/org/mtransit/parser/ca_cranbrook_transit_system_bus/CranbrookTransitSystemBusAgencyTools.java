package org.mtransit.parser.ca_cranbrook_transit_system_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.regex.Pattern;

// https://www.bctransit.com/open-data
// https://www.bctransit.com/cranbrook/home ONLY
public class CranbrookTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CranbrookTransitSystemBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Cranbrook TS";
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		if (Integer.parseInt(gRoute.getRouteShortName()) >= 20) {
			return EXCLUDE; // not Cranbrook (Columbia Valley, Elk Valley or Kimberley)
		}
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // route ID used by GTFS RT
	}

	@Override
	public @Nullable String getRouteIdCleanupRegex() {
		return "\\-[A-Z]+$";
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	// private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final int rsn = Integer.parseInt(gRoute.getRouteShortName());
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
		case 21: return "014A8C"; // Kimberley TS
		case 22: return "8DCD3E"; // Kimberley TS
		case 23: return "F68B1F"; // Kimberley TS
		case 31: return "014A8C"; // Columbia Valley TS
		case 32: return "8DCD3E"; // Columbia Valley TS
		case 41: return "014A8C"; // Elk Valley TS
		// @formatter:on
		}
		throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern ENDS_WITH_DASH_DOWNTOWN = Pattern.compile("( - downtown$)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = ENDS_WITH_DASH_DOWNTOWN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
