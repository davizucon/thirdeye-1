import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute } from "../../utils/routes/routes.util";

const SubscriptionGroupsRouter = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-router" */ "../subscription-groups/subscription-groups.router"
    ).then((module) => ({ default: module.SubscriptionGroupsRouter }))
);

const DatasetsRouter = lazy(() =>
    import(
        /* webpackChunkName: "datasets-router" */ "../datasets/datasets.router"
    ).then((module) => ({ default: module.DatasetsRouter }))
);

const DatasourcesRouter = lazy(() =>
    import(
        /* webpackChunkName: "datasources-router" */ "../datasources/datasources.router"
    ).then((module) => ({ default: module.DatasourcesRouter }))
);

const MetricsRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-router" */ "../metrics/metrics.router"
    ).then((module) => ({ default: module.MetricsRouter }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const ConfigurationRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Configuration path */}
                <Route exact path={AppRoute.CONFIGURATION}>
                    <Redirect to={AppRoute.SUBSCRIPTION_GROUPS} />
                </Route>

                {/* Direct all subscription groups paths to subscription groups router */}
                <Route
                    component={SubscriptionGroupsRouter}
                    path={AppRoute.SUBSCRIPTION_GROUPS}
                />

                {/* Direct all datasets paths to datasets router */}
                <Route component={DatasetsRouter} path={AppRoute.DATASETS} />

                {/* Direct all datasource paths to datasources router */}
                <Route
                    component={DatasourcesRouter}
                    path={AppRoute.DATASOURCES}
                />

                {/* Direct all metrics paths to metrics router */}
                <Route component={MetricsRouter} path={AppRoute.METRICS} />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
