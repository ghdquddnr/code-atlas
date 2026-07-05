import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function normalize(value: unknown) {
  return String(value ?? "").trim().toLowerCase();
}

export function includesQuery(value: unknown, query: string) {
  return !query || normalize(value).includes(query);
}
