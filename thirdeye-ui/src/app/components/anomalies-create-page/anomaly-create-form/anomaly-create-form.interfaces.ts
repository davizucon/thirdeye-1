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

import type { Dispatch, SetStateAction } from "react";
import { Metric } from "../../../rest/dto/metric.interfaces";
// import type { FORM_KEYS } from "./anomaly-create-form.utils";

// export type FormKey = typeof FORM_KEYS[number];
export type FormKey =
    | "alert"
    | "dataset"
    | "datasource"
    | "type"
    | "severity"
    | "metrics"
    | "current"
    | "baseline"
    | "start_date"
    | "end_date";
export type FormState = Record<FormKey, string | number | null>;
export type SelectKeys = "alert" | "type" | "severity" | "metrics";

export type DateKeys = "start_date" | "end_date";

export type BaseModelData = { name: string; id: number };
export type SelectOption = { label: string; value: number | string };

export interface FormRenderData {
    key: FormKey;
    label: string;
    helperLabel?: string;
    type: "text" | "date" | "select";
    isReadOnly?: (params: {
        formState: FormState;
        metrics: Metric[] | null;
    }) => boolean;
}

export interface AnomalyCreateFormProps {
    formState: FormState;
    setFormState: Dispatch<SetStateAction<FormState>>;
}
