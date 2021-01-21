import { cloneDeep } from "lodash";
import create, { GetState, SetState } from "zustand";
import { persist } from "zustand/middleware";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range-util/time-range-util";
import { TimeRangeStore } from "./time-range-store.interfaces";

const LOCAL_STORAGE_KEY_TIME_RANGE = "LOCAL_STORAGE_KEY_TIME_RANGE";
const MAX_ENTRIES_RECENT_CUSTOM_TIME_RANGE_DURATIONS = 3;

// App store for time range, persisted in browser local storage
export const useTimeRangeStore = create<TimeRangeStore>(
    persist<TimeRangeStore>(
        (set: SetState<TimeRangeStore>, get: GetState<TimeRangeStore>) => ({
            timeRangeDuration: getDefaultTimeRangeDuration(),
            recentCustomTimeRangeDurations: [],

            setTimeRangeDuration: (
                timeRangeDuration: TimeRangeDuration
            ): void => {
                if (!timeRangeDuration) {
                    return;
                }

                set({
                    timeRangeDuration: timeRangeDuration,
                });

                if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Add to recent custom time range durations
                    const { recentCustomTimeRangeDurations } = get();
                    const newRecentCustomTimeRangeDurations = [
                        ...recentCustomTimeRangeDurations,
                        timeRangeDuration,
                    ];

                    // Trim recent custom time range duration entries to set threshold
                    if (
                        newRecentCustomTimeRangeDurations.length >
                        MAX_ENTRIES_RECENT_CUSTOM_TIME_RANGE_DURATIONS
                    ) {
                        newRecentCustomTimeRangeDurations.splice(
                            0,
                            newRecentCustomTimeRangeDurations.length -
                                MAX_ENTRIES_RECENT_CUSTOM_TIME_RANGE_DURATIONS
                        );
                    }

                    set({
                        recentCustomTimeRangeDurations: newRecentCustomTimeRangeDurations,
                    });
                }
            },

            refreshTimeRange: (): void => {
                const { timeRangeDuration: appTimeRangeDuration } = get();

                if (appTimeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Custom time range, set as is
                    set({
                        timeRangeDuration: cloneDeep(appTimeRangeDuration),
                    });

                    return;
                }

                // Predefined time range, set current calculated time range duration
                set({
                    timeRangeDuration: getTimeRangeDuration(
                        appTimeRangeDuration.timeRange
                    ),
                });
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_TIME_RANGE, // Persist in browser local storage
        }
    )
);