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
import { Box, Button, Grid } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { Alert } from "@material-ui/lab";
import { isEqual } from "lodash";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as ChartSkeleton } from "../../../../../assets/images/chart-skeleton.svg";
import { EnumerationItemConfig } from "../../../../rest/dto/alert.interfaces";
import { DetectionEvaluation } from "../../../../rest/dto/detection.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../../utils/alerts/alerts.util";
import { PREVIEW_CHART_TEST_IDS } from "../../../alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";
import { useAlertWizardV2Styles } from "../../../alert-wizard-v2/alert-wizard-v2.styles";
import { generateChartOptionsForAlert } from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { EnumerationItemsTable } from "../enumeration-items-table/enumeration-items-table.component";
import { usePreviewChartStyles } from "../preview-chart.styles";
import { ChartContentProps } from "./chart-content.interfaces";

export const ChartContent: FunctionComponent<ChartContentProps> = ({
    alertEvaluation,
    showLoadButton,
    onReloadClick,
    showOnlyActivity,
    onAlertPropertyChange,
    alert,
}) => {
    const sharedWizardClasses = useAlertWizardV2Styles();
    const previewChartClasses = usePreviewChartStyles();
    const { t } = useTranslation();

    const detectionEvaluations = useMemo(() => {
        if (alertEvaluation) {
            return extractDetectionEvaluation(alertEvaluation);
        }

        return [];
    }, [alertEvaluation]);

    const [workingDetectionEvaluations, setWorkingDetectionEvaluations] =
        useState(detectionEvaluations);

    const firstTimeSeriesOptions = useMemo(() => {
        if (
            !workingDetectionEvaluations ||
            workingDetectionEvaluations.length === 0
        ) {
            return null;
        }

        const timeseriesConfiguration = generateChartOptionsForAlert(
            workingDetectionEvaluations[0],
            showOnlyActivity ? [] : workingDetectionEvaluations[0].anomalies,
            t,
            undefined,
            determineTimezoneFromAlertInEvaluation(
                alertEvaluation?.alert.template
            ),
            shouldHideTimeInDatetimeFormat(alertEvaluation?.alert.template),
            showOnlyActivity,
            false,
            showOnlyActivity
        );

        timeseriesConfiguration.brush = false;
        timeseriesConfiguration.zoom = true;
        timeseriesConfiguration.svgContainerUseAuto = true;

        if (showOnlyActivity) {
            timeseriesConfiguration.legend = false;
        }

        return timeseriesConfiguration;
    }, [workingDetectionEvaluations]);

    const handleDeleteEnumerationItemClick = (
        detectionEvaluation: DetectionEvaluation
    ): void => {
        const currentEnumerations: EnumerationItemConfig[] = alert
            .templateProperties.enumerationItems as EnumerationItemConfig[];
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                enumerationItems: currentEnumerations.filter((c) => {
                    return !isEqual(
                        c.params,
                        detectionEvaluation?.enumerationItem?.params
                    );
                }),
            },
        });

        workingDetectionEvaluations &&
            setWorkingDetectionEvaluations((current) =>
                current.filter((c) => {
                    return !isEqual(
                        c.enumerationItem,
                        detectionEvaluation?.enumerationItem
                    );
                })
            );
    };

    return (
        <>
            <Box marginTop={1} minHeight={100} position="relative">
                {!alertEvaluation && (
                    <Box marginTop={1} position="relative">
                        <Box className={previewChartClasses.alertContainer}>
                            <Box position="absolute" width="100%">
                                <Grid container justifyContent="space-around">
                                    <Grid item>
                                        <Alert
                                            className={
                                                sharedWizardClasses.infoAlert
                                            }
                                            severity="info"
                                        >
                                            {t(
                                                "message.please-complete-the-missing-information-to-see-preview"
                                            )}
                                        </Alert>
                                    </Grid>
                                </Grid>
                            </Box>
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
                                        data-testid={
                                            PREVIEW_CHART_TEST_IDS.PREVIEW_BUTTON
                                        }
                                        disabled={!showLoadButton}
                                        variant="text"
                                        onClick={onReloadClick}
                                    >
                                        <RefreshIcon fontSize="large" />
                                        {t("label.load-chart")}
                                    </Button>
                                </Grid>
                            </Grid>
                        </Box>
                        <Box width="100%">
                            <ChartSkeleton />
                        </Box>
                    </Box>
                )}

                {workingDetectionEvaluations?.length === 1 &&
                    firstTimeSeriesOptions && (
                        <Box marginTop={1}>
                            <TimeSeriesChart
                                height={300}
                                {...firstTimeSeriesOptions}
                            />
                        </Box>
                    )}

                {workingDetectionEvaluations &&
                    workingDetectionEvaluations.length > 1 && (
                        <Box marginTop={1}>
                            <EnumerationItemsTable
                                showOnlyActivity
                                detectionEvaluations={
                                    workingDetectionEvaluations
                                }
                                hideTime={shouldHideTimeInDatetimeFormat(
                                    alertEvaluation?.alert.template
                                )}
                                timezone={determineTimezoneFromAlertInEvaluation(
                                    alertEvaluation?.alert.template
                                )}
                                onDeleteClick={handleDeleteEnumerationItemClick}
                            />
                        </Box>
                    )}
            </Box>
        </>
    );
};
