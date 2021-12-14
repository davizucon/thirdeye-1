import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppRoute } from "../../utils/routes/routes.util";

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const RootCauseAnalysisRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.root-cause-analysis"),
                onClick: () =>
                    history.push(AppRoute.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY),
            },
        ]);
        setLoading(false);
    }, []);

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Root cause for an anomaly path */}
                <Route exact path={AppRoute.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY} />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
