import { inject } from "@angular/core";
import { Store } from "@ngxs/store";
import { FabxState } from "./fabx-state";
import { Router } from "@angular/router";

export const authGuard = () => {
    return inject(Store).selectSnapshot(FabxState.isAuthenticated) || inject(Router).navigate(['/login']);
}
