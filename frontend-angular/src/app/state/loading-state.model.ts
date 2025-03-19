import { HttpErrorResponse } from "@angular/common/http";

export type LoadingStateTag = "LOADING" | "FINISHED" | "ERROR"

export type Loading = { tag: "LOADING" }
export type Finished<T> = { tag: "FINISHED", value: T }
export type Error = { tag: "ERROR", err: HttpErrorResponse }

export type LoadingState<T> = Loading | Finished<T> | Error;

export function getFinishedValueOrDefault<T>(ls: LoadingState<T>, orDefault: T): T {
    if (ls.tag === "FINISHED") {
        return ls.value;
    } else {
        return orDefault;
    }
}
