/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, Grid, Typography } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { Alert } from "@material-ui/lab";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { ReactComponent as ChartSkeleton } from "../../../../../assets/images/chart-skeleton.svg";
import {
    NotificationTypeV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../../rest/alerts/alerts.actions";
import { AlertEvaluation } from "../../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../../utils/alerts/alerts.util";
import { TimeRangeButtonWithContext } from "../../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeries } from "../../../visualizations/alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.component";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2.styles";
import {
    MessageDisplayState,
    PreviewChartProps,
} from "./preview-chart.interfaces";
import { usePreviewChartStyles } from "./preview-chart.styles";

export const PreviewChart: FunctionComponent<PreviewChartProps> = ({
    alert,
    displayState,
    subtitle,
}) => {
    const sharedWizardClasses = useAlertWizardV2Styles();
    const previewChartClasses = usePreviewChartStyles();
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );
    const { notify } = useNotificationProviderV1();
    const [currentAlertEvaluation, setCurrentAlertEvaluation] =
        useState<AlertEvaluation>();

    const {
        evaluation,
        getEvaluation,
        errorMessages: getEvaluationRequestErrors,
        status: getEvaluationStatus,
    } = useGetEvaluation();

    const fetchAlertEvaluation = async (
        start: number,
        end: number
    ): Promise<void> => {
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(alert, start, end)
        );

        if (fetchedAlertEvaluation === undefined) {
            setCurrentAlertEvaluation(undefined);
        }

        setCurrentAlertEvaluation(fetchedAlertEvaluation);
    };

    useEffect(() => {
        if (getEvaluationStatus === ActionStatus.Error) {
            !isEmpty(getEvaluationRequestErrors)
                ? getEvaluationRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.chart-data"),
                      })
                  );
        }
    }, [getEvaluationRequestErrors, getEvaluationStatus]);

    useEffect(() => {
        // If alert changes, reset the evaluation data
        setCurrentAlertEvaluation(undefined);
    }, [alert]);

    return (
        <>
            <Grid item xs={12}>
                <Box marginBottom={2}>
                    <Typography variant="h6">
                        {t("label.alert-preview")}
                    </Typography>
                    <Typography variant="body2">{subtitle}</Typography>
                </Box>
            </Grid>
            <Grid item xs={12}>
                {displayState === MessageDisplayState.SELECT_TEMPLATE && (
                    <Box position="relative">
                        <Box className={previewChartClasses.alertContainer}>
                            <Grid container justifyContent="space-around">
                                <Grid item>
                                    <Alert
                                        className={
                                            sharedWizardClasses.infoAlert
                                        }
                                        severity="info"
                                    >
                                        {t(
                                            "message.select-a-template-to-preview"
                                        )}
                                    </Alert>
                                </Grid>
                            </Grid>
                        </Box>
                        <Box width="100%">
                            <ChartSkeleton />
                        </Box>
                    </Box>
                )}
                {displayState ===
                    MessageDisplayState.FILL_TEMPLATE_PROPERTY_VALUES && (
                    <Box position="relative">
                        <Box className={previewChartClasses.alertContainer}>
                            <Grid container justifyContent="space-around">
                                <Grid item>
                                    <Alert
                                        className={
                                            sharedWizardClasses.warningAlert
                                        }
                                        severity="warning"
                                    >
                                        {t(
                                            "message.complete-missing-information-to-see-preview"
                                        )}
                                    </Alert>
                                </Grid>
                            </Grid>
                        </Box>
                        <Box width="100%">
                            <ChartSkeleton />
                        </Box>
                    </Box>
                )}
                {displayState === MessageDisplayState.GOOD_TO_PREVIEW && (
                    <Box minHeight={100} position="relative">
                        {getEvaluationStatus === ActionStatus.Working && (
                            <SkeletonV1
                                animation="pulse"
                                height={300}
                                variant="rect"
                            />
                        )}

                        {getEvaluationStatus !== ActionStatus.Working && (
                            <>
                                {!currentAlertEvaluation && (
                                    <Box position="relative">
                                        <Box
                                            className={
                                                previewChartClasses.alertContainer
                                            }
                                        >
                                            <Grid
                                                container
                                                alignItems="center"
                                                className={
                                                    previewChartClasses.heightWholeContainer
                                                }
                                                justifyContent="space-around"
                                            >
                                                <Grid item>
                                                    <Button
                                                        color="primary"
                                                        variant="text"
                                                        onClick={() => {
                                                            fetchAlertEvaluation(
                                                                startTime,
                                                                endTime
                                                            );
                                                        }}
                                                    >
                                                        <RefreshIcon fontSize="large" />
                                                    </Button>
                                                </Grid>
                                            </Grid>
                                        </Box>
                                        <Box width="100%">
                                            <ChartSkeleton />
                                        </Box>
                                    </Box>
                                )}

                                {currentAlertEvaluation && (
                                    <Box>
                                        <Grid container>
                                            <Grid item sm={8} xs={12}>
                                                <TimeRangeButtonWithContext
                                                    onTimeRangeChange={(
                                                        start,
                                                        end
                                                    ) =>
                                                        fetchAlertEvaluation(
                                                            start,
                                                            end
                                                        )
                                                    }
                                                />
                                            </Grid>
                                            <Grid item sm={4} xs={12}>
                                                <Box textAlign="right">
                                                    <Button
                                                        color="primary"
                                                        variant="outlined"
                                                        onClick={() => {
                                                            fetchAlertEvaluation(
                                                                startTime,
                                                                endTime
                                                            );
                                                        }}
                                                    >
                                                        <RefreshIcon fontSize="small" />
                                                        {t(
                                                            "label.reload-preview"
                                                        )}
                                                    </Button>
                                                </Box>
                                            </Grid>
                                        </Grid>
                                        <Box height={300} marginTop={2}>
                                            <AlertEvaluationTimeSeries
                                                hideBrush
                                                alertEvaluation={evaluation}
                                            />
                                        </Box>
                                    </Box>
                                )}
                            </>
                        )}
                    </Box>
                )}
            </Grid>
        </>
    );
};
