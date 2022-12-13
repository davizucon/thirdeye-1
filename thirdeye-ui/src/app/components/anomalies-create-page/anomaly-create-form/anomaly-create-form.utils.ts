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
import { capitalize, lowerCase } from "lodash";
import type { Alert } from "../../../rest/dto/alert.interfaces";
import {
    AnomalySeverity,
    AnomalyType,
} from "../../../rest/dto/anomaly.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import { generateDateRangeMonthsFromNow } from "../../../utils/routes/routes.util";
import {
    BaseModelData,
    FormKey,
    FormRenderData,
    SelectKeys,
    SelectOption,
} from "./anomaly-create-form.interfaces";

export const FORM_KEYS = [
    "alert",
    "dataset",
    "datasource",
    "type",
    "severity",
    "metrics",
    "current",
    "baseline",
    "start_date",
    "end_date",
] as const;

export const getInitialState = (): Record<FormKey, string | number | null> => {
    const [start_date, end_date] = generateDateRangeMonthsFromNow(0);
    const customInitialValues: Partial<Record<FormKey, number | undefined>> = {
        start_date,
        end_date,
    };

    return Object.assign(
        {},
        ...FORM_KEYS.map((f) => ({
            [f]: customInitialValues?.[f] ?? null,
        }))
    );
};

const generateModelSelectOptions = <T extends BaseModelData>(
    list: T[] | null
): SelectOption[] =>
    (list || []).map(({ id, name }) => ({ value: id, label: name }));

const formatEnumLabel = (key: string): string => capitalize(lowerCase(key));

const generateEnumSelectOptions = (list: string[]): SelectOption[] =>
    (list || []).map((item) => ({ value: item, label: formatEnumLabel(item) }));

export const getSelectOptions = ({
    alerts,
    metrics,
}: {
    alerts: Alert[] | null;
    metrics: Metric[] | null;
}): Record<SelectKeys, SelectOption[]> => {
    return {
        alert: generateModelSelectOptions<Alert>(alerts),
        metrics: generateModelSelectOptions<Metric>(metrics),
        type: generateEnumSelectOptions(Object.keys(AnomalyType)),
        severity: generateEnumSelectOptions(Object.keys(AnomalySeverity)),
    };
};

export const FORM_ENUM: Record<FormKey, FormRenderData> = {
    alert: {
        key: "alert",
        label: "Alert",
        type: "select",
    },
    datasource: {
        key: "datasource",
        label: "Datasource",
        type: "text",
        isReadOnly: () => true,
        helperLabel: "Picked from the selected alert",
    },
    dataset: {
        key: "dataset",
        label: "Dataset",
        type: "text",
        isReadOnly: () => true,
        helperLabel: "Picked from the selected alert",
    },
    metrics: {
        key: "metrics",
        label: "Metrics",
        type: "select",
        isReadOnly: ({ metrics }) => !(metrics && metrics?.length > 0),
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
    type: {
        key: "type",
        label: "Anomaly Type",
        type: "select",
    },
    severity: {
        key: "type",
        label: "Anomaly Severity",
        type: "select",
    },
};
