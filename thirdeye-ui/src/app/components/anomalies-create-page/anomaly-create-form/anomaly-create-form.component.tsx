/*
 * Copyright 2022 StarTree Inc
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
import LuxonUtils from "@date-io/luxon";
import {
    Box,
    FormControl,
    Grid,
    MenuItem,
    Select,
    TextField,
    Typography,
} from "@material-ui/core";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import { capitalize } from "lodash";
import React, {
    ChangeEvent,
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlerts } from "../../../rest/alerts/alerts.actions";
import { useGetAnomalies } from "../../../rest/anomalies/anomaly.actions";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import { useGetMetrics } from "../../../rest/metrics/metrics.actions";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { InputSectionProps } from "../../form-basics/input-section/input-section.interfaces";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { DateTimePickerToolbar } from "../../time-range/time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import type {
    AnomalyCreateFormProps,
    FormKey,
    FormRenderData,
    FormState,
    SelectKeys,
} from "./anomaly-create-form.interfaces";
import {
    FORM_ENUM,
    FORM_KEYS,
    getInitialState,
    getSelectOptions,
} from "./anomaly-create-form.utils";

const getSelectedAlert = (
    alerts: Alert[] | null,
    alertId: number
): Alert | null => alerts?.find((a) => a.id === alertId) ?? null;

const getMetricsForDataset = (
    metrics: Metric[] | null,
    datasetName: string
): Metric[] | null =>
    metrics?.filter((m) => m.dataset.name === datasetName) ?? null;

export const AnomalyCreateForm: FunctionComponent<AnomalyCreateFormProps> =
    () => {
        const [formState, setFormState] = useState<FormState>(
            getInitialState()
        );
        const { t } = useTranslation();

        const { alerts, getAlerts, status: alertsFetchStatus } = useGetAlerts();
        const { getAnomalies } = useGetAnomalies();
        const { metrics, getMetrics } = useGetMetrics();

        const selectedAlerts = getSelectedAlert(
            alerts,
            formState.alert as number
        );

        const metricsForAlert = formState.dataset
            ? getMetricsForDataset(metrics, formState.dataset as string)
            : null;

        console.log({ selectedAlerts });
        console.log({ metricsForAlert });

        useEffect(() => {
            getAlerts();
            getMetrics();
        }, []);

        useEffect(() => {
            const {
                alert: alertId,
                start_date: startTime,
                end_date: endTime,
            } = formState;

            if (alertId) {
                getAnomalies({
                    ...(alertId && {
                        alertId,
                    }),
                    // ...(startTime && {
                    //     startTime,
                    // }),
                    // ...(endTime && {
                    //     endTime,
                    // }),
                } as Record<string, number>).then(console.log);
            }
        }, [formState]);

        const selectOptions = useMemo(
            () => getSelectOptions({ alerts, metrics: metricsForAlert }),
            [alerts, metricsForAlert]
        );

        const setField = (
            fieldKey: FormKey,
            fieldValue: string | number | null = null
        ): void => {
            setFormState((formStateProps) => ({
                ...formStateProps,
                [fieldKey]: fieldValue ?? null,
            }));
        };

        const handleTextChange = (
            event: ChangeEvent<HTMLInputElement>
        ): void => {
            const { name, value } = event.target;
            setField(name as FormKey, value);
        };

        const handleDateChange = (
            formKeyProp: FormKey,
            dateProp: MaterialUiPickersDate
        ): void => {
            setField(formKeyProp, dateProp?.valueOf());
        };

        const handleSelectChange = (
            event: ChangeEvent<{ name?: string; value: number | unknown }>
        ): void => {
            const { name, value } = event.target as {
                name: FormKey;
                value: number | string;
            };
            setField(name as FormKey, value as number);

            // Specific field logic
            if ((name as FormKey) === "alert") {
                const alertData = alerts?.find((a) => a.id === value);
                if (!alertData) {
                    return;
                }

                // Names of dataSource and dataset
                const { dataSource, dataset } = alertData.templateProperties;

                setField("datasource", dataSource as string);
                setField("dataset", dataset as string);
                setField("metrics", null);
            }
        };

        const getInputComponent = useCallback(
            ({
                fieldType,
                formKey,
                readOnly = false,
                value,
            }: {
                fieldType: FormRenderData["type"];
                formKey: FormKey;
                readOnly: boolean;
                value: FormState[FormKey];
            }) => {
                if (fieldType === "date") {
                    return (
                        <MuiPickersUtilsProvider utils={LuxonUtils}>
                            <DateTimePicker
                                autoOk
                                fullWidth
                                ToolbarComponent={DateTimePickerToolbar}
                                readOnly={readOnly}
                                // error={Boolean(
                                //     formErrors.startTime
                                // )}
                                // helperText={
                                //     formErrors.startTime &&
                                //     formErrors.startTime.message
                                // }
                                value={new Date(value || Date.now())}
                                variant="inline"
                                onChange={(dateProp) =>
                                    handleDateChange(formKey, dateProp)
                                }
                            />
                        </MuiPickersUtilsProvider>
                    );
                }

                if (fieldType === "select") {
                    return (
                        <FormControl
                            fullWidth
                            // error={Boolean(
                            //     errors.dataSource?.name
                            //         ?.message
                            // )}
                            size="small"
                            variant="outlined"
                        >
                            <Select
                                fullWidth
                                required
                                name={formKey}
                                readOnly={readOnly}
                                type="string"
                                value={value}
                                variant="outlined"
                                onChange={handleSelectChange}
                            >
                                {selectOptions[formKey as SelectKeys].map(
                                    ({ label, value }) => (
                                        <MenuItem key={value} value={value}>
                                            {label}
                                        </MenuItem>
                                    )
                                )}
                            </Select>
                        </FormControl>
                    );
                }

                return (
                    <TextField
                        fullWidth
                        required
                        disabled={readOnly}
                        name={formKey}
                        type="string"
                        value={value}
                        variant="outlined"
                        onChange={handleTextChange}
                    />
                );
            },
            [selectOptions]
        );

        return (
            <PageContentsCardV1>
                <Grid container>
                    <Grid item xs={12}>
                        <Typography variant="h5">
                            {capitalize(
                                t("message.entity-properties", {
                                    entity: t("anomaly"),
                                })
                            )}
                        </Typography>
                    </Grid>

                    <Grid container item alignItems="center" xs={12}>
                        <LoadingErrorStateSwitch
                            isError={alertsFetchStatus === ActionStatus.Error}
                            isLoading={
                                alertsFetchStatus === ActionStatus.Working
                            }
                            loadingState={
                                <Box mx={1}>
                                    {Object.keys(FORM_ENUM).map((_, index) => (
                                        <SkeletonV1
                                            height={50}
                                            key={index}
                                            width="450px"
                                        />
                                    ))}
                                </Box>
                            }
                        >
                            {FORM_KEYS.map((formKey) => {
                                const {
                                    type: fieldType,
                                    isReadOnly,
                                    helperLabel,
                                } = FORM_ENUM[formKey];

                                const value = formState[formKey];

                                const inputSectionProps: InputSectionProps = {
                                    helperLabel,
                                    label: FORM_ENUM[formKey].label,
                                    inputComponent: getInputComponent({
                                        fieldType,
                                        formKey,
                                        readOnly: !!isReadOnly?.({
                                            formState: formState as FormState,
                                            metrics: metricsForAlert,
                                        }),
                                        value,
                                    }),
                                };

                                return (
                                    <InputSection
                                        {...inputSectionProps}
                                        key={formKey}
                                    />
                                );

                                // if (type === "date") {
                                //     return (
                                //         <InputSection
                                //             // helperLabel={helperText}
                                //             {...inputSectionProps}
                                //             inputComponent={
                                //                 <MuiPickersUtilsProvider
                                //                     utils={LuxonUtils}
                                //                 >
                                //                     <DateTimePicker
                                //                         autoOk
                                //                         fullWidth
                                //                         ToolbarComponent={
                                //                             DateTimePickerToolbar
                                //                         }
                                //                         readOnly={readonly}
                                //                         // error={Boolean(
                                //                         //     formErrors.startTime
                                //                         // )}
                                //                         // helperText={
                                //                         //     formErrors.startTime &&
                                //                         //     formErrors.startTime.message
                                //                         // }
                                //                         value={new Date(value)}
                                //                         variant="inline"
                                //                         onChange={handleDateChange}
                                //                         // onChange={(e) => {
                                //                         //     onChange(e?.valueOf());
                                //                         // }}
                                //                     />
                                //                 </MuiPickersUtilsProvider>
                                //             }
                                //             key={formKey}
                                //             // label={t("label.type")}
                                //             // label={FORM_ENUM[formKey].label}
                                //         />
                                //     );
                                // }

                                // if (type === "select") {
                                //     return (
                                //         <InputSection
                                //             {...inputSectionProps}
                                //             // inputSectionProps
                                //             // helperLabel={helperText}
                                //             // fullWidth
                                //             inputComponent={
                                //                 <FormControl
                                //                     fullWidth
                                //                     // error={Boolean(
                                //                     //     errors.dataSource?.name
                                //                     //         ?.message
                                //                     // )}
                                //                     size="small"
                                //                     variant="outlined"
                                //                 >
                                //                     <Select
                                //                         fullWidth
                                //                         required
                                //                         name={formKey}
                                //                         // label={`Enter ${FORM_ENUM[
                                //                         //     formKey
                                //                         // ].label.toLowerCase()} for anomaly`}
                                //                         readOnly={readonly}
                                //                         // placeholder={`Enter ${FORM_ENUM[
                                //                         //     formKey
                                //                         // ].label.toLowerCase()} for anomaly`}
                                //                         type="string"
                                //                         value={value}
                                //                         variant="outlined"
                                //                         onChange={handleSelectChange}
                                //                     >
                                //                         {/* <MenuItem
                                //                             // disabled
                                //                             selected
                                //                             // value={null}
                                //                         >
                                //                             Select a {formKey}
                                //                         </MenuItem> */}
                                //                         {selectOptions[
                                //                             formKey as SelectKeys
                                //                         ].map(({ label, value }) => (
                                //                             <MenuItem
                                //                                 key={value}
                                //                                 value={value}
                                //                             >
                                //                                 {label}
                                //                             </MenuItem>
                                //                         ))}
                                //                     </Select>
                                //                 </FormControl>
                                //             }
                                //             key={formKey}
                                //             // label={t("label.type")}
                                //             // label={FORM_ENUM[formKey].label}
                                //         />
                                //     );
                                // }

                                // return (
                                //     <InputSection
                                //         {...inputSectionProps}
                                //         // helperLabel={helperText}
                                //         // fullWidth
                                //         inputComponent={
                                //             <TextField
                                //                 fullWidth
                                //                 required
                                //                 disabled={readonly}
                                //                 // inputRef={formRegister}
                                //                 name={formKey}
                                //                 // placeholder={`Enter ${FORM_ENUM[
                                //                 //     formKey
                                //                 // ].label.toLowerCase()} for anomaly`}
                                //                 // placeholder={t(
                                //                 //     "label.enter-a-type-event"
                                //                 // )}
                                //                 type="string"
                                //                 value={value}
                                //                 variant="outlined"
                                //                 onChange={handleTextChange}
                                //             />
                                //         }
                                //         key={formKey}
                                //         // label={t("label.type")}
                                //         // label={FORM_ENUM[formKey].label}
                                //     />
                                // );
                            })}
                        </LoadingErrorStateSwitch>
                    </Grid>
                </Grid>
            </PageContentsCardV1>
        );
    };
