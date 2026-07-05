import * as React from "react";
import { cn } from "../../lib/utils";

export function Select({ className, ...props }: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      className={cn(
        "min-h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none transition focus:border-blue-700 focus:ring-2 focus:ring-blue-100",
        className
      )}
      {...props}
    />
  );
}
