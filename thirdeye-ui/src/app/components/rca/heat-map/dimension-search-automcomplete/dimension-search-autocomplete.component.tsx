/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Chip, InputAdornment, TextField } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { Autocomplete } from "@material-ui/lab";
import { isString, pull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { EMPTY_STRING_DISPLAY } from "../../../../utils/anomalies/anomalies.util";
import { AnomalyFilterOption } from "../heat-map.interfaces";
import { formatDimensionOptions } from "../heat-map.utils";
import { DimensionSearchAutocompleteProps } from "./dimension-search-autocomplete.interfaces";

export const DimensionSearchAutocomplete: FunctionComponent<DimensionSearchAutocompleteProps> =
    ({ heatMapData, anomalyFilters, onFilterChange }) => {
        const { t } = useTranslation();

        const [anomalyFilterOptions, setAnomalyFilterOptions] = useState<
            AnomalyFilterOption[]
        >([]);

        useEffect(() => {
            if (!heatMapData) {
                return;
            }

            if (anomalyFilterOptions.length === 0) {
                setAnomalyFilterOptions(formatDimensionOptions(heatMapData));
            }
        }, [heatMapData]);

        const handleNodeFilterOnDelete = (node: AnomalyFilterOption): void => {
            const resultantFilters = pull(anomalyFilters, node);
            onFilterChange([...resultantFilters]);
        };

        const handleOnChangeFilter = (options: AnomalyFilterOption[]): void => {
            onFilterChange([...options]);
        };

        return (
            <Autocomplete
                freeSolo
                multiple
                getOptionLabel={(option: AnomalyFilterOption) =>
                    isString(option.value)
                        ? option.value || EMPTY_STRING_DISPLAY
                        : ""
                }
                groupBy={(option: AnomalyFilterOption) =>
                    isString(option.key) ? option.key : ""
                }
                options={anomalyFilterOptions || []}
                renderInput={(params) => (
                    <TextField
                        {...params}
                        fullWidth
                        InputProps={{
                            ...params.InputProps,
                            /**
                             * Ensure that the chips are also
                             * rendered which are adornments
                             */
                            startAdornment: (
                                <>
                                    <InputAdornment position="start">
                                        <SearchIcon />
                                    </InputAdornment>
                                    {params.InputProps.startAdornment}
                                </>
                            ),
                        }}
                        placeholder={t("message.anomaly-filter-search")}
                        variant="outlined"
                    />
                )}
                renderTags={() =>
                    anomalyFilters.map((option: AnomalyFilterOption, index) => (
                        <Chip
                            className="filter-chip"
                            key={`${index}_${option.value}`}
                            label={`${option.key}=${
                                option.value || EMPTY_STRING_DISPLAY
                            }`}
                            size="small"
                            style={{
                                marginTop: 0,
                                marginBottom: 0,
                                marginRight: 3,
                            }}
                            onDelete={() => handleNodeFilterOnDelete(option)}
                        />
                    ))
                }
                value={anomalyFilters}
                onChange={(_e, options) =>
                    handleOnChangeFilter(options as AnomalyFilterOption[])
                }
            />
        );
    };
