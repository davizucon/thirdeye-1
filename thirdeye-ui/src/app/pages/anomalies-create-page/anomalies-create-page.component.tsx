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
import { Box, Button, Grid, TextField, Typography } from "@material-ui/core";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import React, {
    ChangeEvent,
    FormEvent,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { InputSection } from "../../components/form-basics/input-section/input-section.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { DateTimePickerToolbar } from "../../components/time-range/time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
} from "../../platform/components";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { useGetDatasets } from "../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import { generateDateRangeMonthsFromNow } from "../../utils/routes/routes.util";

const FORM_ID_ANOMALY_PROPERTIES = "FORM_ID_ANOMALY_PROPERTIES";

const FORM_KEYS = [
    "dataset",
    "datasource",
    "metrics",
    "current",
    "baseline",
    "start_date",
    "end_date",
    "anomaly_type",
    "alert",
] as const;

type FormKey = typeof FORM_KEYS[number];

type ForeignKeys = "dataset" | "datasource" | "alert";

type DateKeys = "start_date" | "end_date";

const getInitialState = (): Record<FormKey, string | number> => {
    const [start_date, end_date] = generateDateRangeMonthsFromNow(0);
    const customInitialValues: Partial<Record<DateKeys, number | undefined>> = {
        start_date,
        end_date,
    };

    return Object.assign(
        {},
        ...FORM_KEYS.map((f) => ({
            [f]:
                customInitialValues?.[f as keyof typeof customInitialValues] ??
                "",
        }))
    );
};

interface FormRenderData {
    key: FormKey;
    label: string;
    type: "text" | "date" | "foreignKey";
}

const FORM_ENUM: Record<FormKey, FormRenderData> = {
    dataset: {
        key: "dataset",
        label: "Dataset",
        type: "text",
    },
    datasource: {
        key: "datasource",
        label: "Datasource",
        type: "text",
    },
    metrics: {
        key: "metrics",
        label: "Metrics",
        type: "text",
    },
    current: {
        key: "current",
        label: "Current",
        type: "text",
    },
    baseline: {
        key: "baseline",
        label: "Baseline",
        type: "text",
    },
    start_date: {
        key: "start_date",
        label: "Start Date",
        type: "date",
    },
    end_date: {
        key: "end_date",
        label: "End Date",
        type: "date",
    },
    anomaly_type: {
        key: "anomaly_type",
        label: "Anomaly Type",
        type: "text",
    },
    alert: {
        key: "alert",
        label: "Alert",
        type: "text",
    },
};

export const AnomaliesCreatePage: FunctionComponent = () => {
    const { t } = useTranslation();

    const [formState, setFormState] = useState<
        Record<FormKey, string | number>
    >(getInitialState());

    // const [start, end] = generateDateRangeMonthsFromNow(0);

    // Dataset
    // DataSource
    // Metrics + Aggregation Function
    // Current (tool tip actual)
    // Baseline (tool tip expected)
    // Start date
    // End date
    // Duration (this can be pre-computed based on)
    // Type of anomaly (Similar to Is this an anomaly)
    // Alert (can be associated as users are submitting an anomaly for a given alert)

    const { alerts, getAlerts, status: alertsFetchStatus } = useGetAlerts();
    const {
        datasources,
        getDatasources,
        status: datasourcesStatus,
    } = useGetDatasources();
    const { datasets, getDatasets, status: datasetsStatus } = useGetDatasets();

    const navigate = useNavigate();

    console.log(datasets, datasources, alerts);
    console.log({ formState });

    useEffect(() => {
        getDatasets();
        getDatasources();
        getAlerts();
    }, []);

    const handleTextChange = (event: ChangeEvent<HTMLInputElement>): void => {
        console.log(event.target.name, event.target.value);
    };

    const handleDateChange = (dateProp: MaterialUiPickersDate): void => {
        console.log(dateProp);
    };

    const handleSubmit = (event: FormEvent): void => {
        event.preventDefault();

        // Duration
        console.log("Submit form", event);
    };

    const onCancel = (): void => {
        navigate(-1);
    };

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.anomaly"),
                })}
            />

            <PageContentsGridV1 fullHeight>
                <Grid
                    container
                    item
                    noValidate
                    component="form"
                    id={FORM_ID_ANOMALY_PROPERTIES}
                    xs={12}
                    onSubmit={handleSubmit}
                    // onSubmit={handleSubmit(onSubmitEventsPropertiesForm)}
                >
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <Grid container>
                                <Grid item xs={12}>
                                    <Typography variant="h5">
                                        {t("label.event-properties")}
                                    </Typography>
                                </Grid>

                                <Grid
                                    // container
                                    item
                                    alignItems="center"
                                    xs={12}
                                >
                                    {FORM_KEYS.map((formKey) => {
                                        const fieldData = FORM_ENUM[formKey];

                                        const value = formState[formKey];

                                        if (fieldData.type === "date") {
                                            return (
                                                <InputSection
                                                    inputComponent={
                                                        <MuiPickersUtilsProvider
                                                            utils={LuxonUtils}
                                                        >
                                                            <DateTimePicker
                                                                autoOk
                                                                fullWidth
                                                                ToolbarComponent={
                                                                    DateTimePickerToolbar
                                                                }
                                                                // error={Boolean(
                                                                //     formErrors.startTime
                                                                // )}
                                                                // helperText={
                                                                //     formErrors.startTime &&
                                                                //     formErrors.startTime.message
                                                                // }
                                                                value={
                                                                    new Date(
                                                                        value
                                                                    )
                                                                }
                                                                variant="inline"
                                                                onChange={
                                                                    handleDateChange
                                                                }
                                                                // onChange={(e) => {
                                                                //     onChange(e?.valueOf());
                                                                // }}
                                                            />
                                                        </MuiPickersUtilsProvider>
                                                    }
                                                    key={formKey}
                                                    // label={t("label.type")}
                                                    label={
                                                        FORM_ENUM[formKey].label
                                                    }
                                                />
                                            );
                                        }

                                        return (
                                            <InputSection
                                                inputComponent={
                                                    <TextField
                                                        fullWidth
                                                        required
                                                        // inputRef={formRegister}
                                                        label={`Enter ${FORM_ENUM[
                                                            formKey
                                                        ].label.toLowerCase()} for anomaly`}
                                                        name={formKey}
                                                        // placeholder={t(
                                                        //     "label.enter-a-type-event"
                                                        // )}
                                                        type="string"
                                                        value={value}
                                                        variant="outlined"
                                                        onChange={
                                                            handleTextChange
                                                        }
                                                    />
                                                }
                                                // fullWidth
                                                key={formKey}
                                                // label={t("label.type")}
                                                label={FORM_ENUM[formKey].label}
                                            />
                                        );
                                    })}
                                </Grid>
                            </Grid>
                        </PageContentsCardV1>
                    </Grid>
                </Grid>
            </PageContentsGridV1>

            {/* Controls */}
            <Box width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button color="secondary" onClick={onCancel}>
                                {t("label.cancel")}
                            </Button>
                        </Grid>

                        <Grid item>
                            <Button color="primary" onClick={handleSubmit}>
                                {t("label.create-entity", {
                                    entity: t("label.anomaly"),
                                })}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
