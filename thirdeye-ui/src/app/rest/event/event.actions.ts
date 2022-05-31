import { useHTTPAction } from "../create-rest-action";
import { Event } from "../dto/event.interfaces";
import {
    GetAllEventsProps,
    GetEvent,
    GetEvents,
    GetEventsForAnomaly,
    GetEventsForAnomalyRestProps,
} from "./events.interfaces";
import {
    getAllEvents as getAllEventsRest,
    getEvent as getEventRest,
    getEventsForAnomaly as getEventsForAnomalyRest,
} from "./events.rest";

export const useGetEvent = (): GetEvent => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Event>(getEventRest);

    const getEvent = (id: number): Promise<Event | undefined> => {
        return makeRequest(id);
    };

    return { event: data, getEvent, status, errorMessages };
};

export const useGetEvents = (): GetEvents => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Event[]>(getAllEventsRest);

    const getEvents = (
        getEventsParams: GetAllEventsProps = {}
    ): Promise<Event[] | undefined> => {
        return makeRequest(getEventsParams);
    };

    return {
        events: data,
        getEvents,
        status,
        errorMessages,
    };
};

export const useGetEventsForAnomaly = (): GetEventsForAnomaly => {
    const { data, makeRequest, status, errorMessages } = useHTTPAction<Event[]>(
        getEventsForAnomalyRest
    );

    const getEventsForAnomaly = (
        getEventsParams: GetEventsForAnomalyRestProps
    ): Promise<Event[] | undefined> => {
        return makeRequest(getEventsParams);
    };

    return {
        events: data,
        getEventsForAnomaly,
        status,
        errorMessages,
    };
};
