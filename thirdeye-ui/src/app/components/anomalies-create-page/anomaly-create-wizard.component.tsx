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

import { Box, Button, Grid } from "@material-ui/core";
import React, { FormEvent, FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../platform/components";
import { AnomalyResultSource } from "../../rest/dto/anomaly.interfaces";
import { AnomalyCreateForm } from "./anomaly-create-form/anomaly-create-form.component";
import { FormState } from "./anomaly-create-form/anomaly-create-form.interfaces";
import { getInitialState } from "./anomaly-create-form/anomaly-create-form.utils";

const FORM_ID_ANOMALY_PROPERTIES = "FORM_ID_ANOMALY_PROPERTIES";

export const AnomalyCreateWizard: FunctionComponent = () => {
    const [formState, setFormState] = useState<FormState>(getInitialState());
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleSubmit = (event: FormEvent): void => {
        event.preventDefault();

        const finalPayload = {
            avgCurrentVal: 0,
            avgBaselineVal: 0,
            score: 0,
            weight: 0,
            impactToGlobal: 0,
            sourceType: AnomalyResultSource.USER_LABELED_ANOMALY,
        };

        console.log("formState:", formState);
        console.log("finalPayload:", finalPayload);

        // Duration
        console.log("Submit form", event);
    };

    const onCancel = (): void => {
        navigate(-1);
    };

    return (
        <>
            {/* Wizard */}
            <PageContentsGridV1 fullHeight>
                <Grid
                    container
                    item
                    noValidate
                    component="form"
                    id={FORM_ID_ANOMALY_PROPERTIES}
                    xs={12}
                    onSubmit={handleSubmit}
                >
                    <Grid item xs={12}>
                        <AnomalyCreateForm
                            formState={formState}
                            setFormState={setFormState}
                        />
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
        </>
    );
};
